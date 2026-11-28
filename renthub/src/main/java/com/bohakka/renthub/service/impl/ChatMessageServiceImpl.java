package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ChatAttachmentFile;
import com.bohakka.renthub.dto.ChatMessageResponse;
import com.bohakka.renthub.entity.ChatMessage;
import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.MessageKind;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.ChatAttachmentUploaded;
import com.bohakka.renthub.event.ChatMessageSent;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.ChatMessageRepository;
import com.bohakka.renthub.service.ChatMessageService;
import com.bohakka.renthub.service.PrivateFileStorage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link ChatMessageService} (D-57). Attachments are checked by kind, size and leading bytes ({@link AttachmentRules},
 * {@link AttachmentFormat}), streamed to the private chat bucket under {@code chat/{conversation}/{uuid}.{ext}} and
 * removed again if the transaction rolls back. Every saved message is pushed to both participants after the commit.
 */
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

	private static final int MAX_PAGE = 50;

	private final ChatMessageRepository messages;
	private final ChatAccess access;
	private final ChatMapper mapper;
	private final PrivateFileStorage storage;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public List<ChatMessageResponse> history(Long userId, Long conversationId, Long beforeId, int size) {
		access.forParticipant(userId, conversationId);
		long before = beforeId == null ? Long.MAX_VALUE : beforeId;
		List<ChatMessageResponse> page = new ArrayList<>(messages
			.findPage(conversationId, before, PageRequest.of(0, Math.clamp(size, 1, MAX_PAGE))).stream()
			.map(mapper::message).toList());
		Collections.reverse(page);
		return page;
	}

	@Override
	@Transactional
	public ChatMessageResponse sendText(Long userId, Long conversationId, String body) {
		Conversation conversation = access.forParticipant(userId, conversationId);
		User sender = sender(conversation, userId);
		return publish(conversation, messages.save(ChatMessage.text(conversation, sender, body.strip(), clock.instant())));
	}

	@Override
	@Transactional
	public ChatMessageResponse sendAttachment(Long userId, Long conversationId, MessageKind kind, MultipartFile file) {
		if (kind == null || kind == MessageKind.TEXT) {
			throw new BusinessRuleException("chat.kind_invalid", "chat.kind_invalid");
		}
		Conversation conversation = access.forParticipant(userId, conversationId);
		User sender = sender(conversation, userId);
		AttachmentRules rules = AttachmentRules.of(kind);
		if (file == null || file.isEmpty()) {
			throw new BusinessRuleException("chat.attachment_empty", "chat.attachment_empty");
		}
		if (file.getSize() > rules.maxBytes()) {
			throw new BusinessRuleException("chat.attachment_too_large", "chat.attachment_too_large",
				rules.maxMegabytes());
		}
		try (InputStream in = new BufferedInputStream(file.getInputStream(), AttachmentFormat.HEADER_BYTES)) {
			in.mark(AttachmentFormat.HEADER_BYTES);
			byte[] header = in.readNBytes(AttachmentFormat.HEADER_BYTES);
			in.reset();
			AttachmentFormat format = AttachmentFormat.detect(header, extension(file.getOriginalFilename()))
				.filter(rules::allows)
				.orElseThrow(() -> new BusinessRuleException("chat.attachment_unsupported", "chat.attachment_unsupported"));
			String key = "chat/%d/%s.%s".formatted(conversation.getId(), UUID.randomUUID(), format.extension());
			String contentType = rules.contentType(format);
			storage.put(key, in, file.getSize(), contentType);
			events.publishEvent(new ChatAttachmentUploaded(key));
			ChatMessage message = ChatMessage.attachment(conversation, sender, kind, key,
				rules.fileName(file.getOriginalFilename(), format), contentType, file.getSize(), clock.instant());
			return publish(conversation, messages.save(message));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ChatAttachmentFile attachment(Long userId, Long messageId) {
		ChatMessage message = messages.findWithConversationById(messageId)
			.filter(ChatMessage::hasAttachment)
			.orElseThrow(() -> new NotFoundException("chat.message_not_found", "chat.message_not_found"));
		message.getConversation().requireParticipant(userId);
		String type = message.getAttachmentType();
		boolean inline = type.startsWith("image/") || type.startsWith("audio/") || type.startsWith("video/")
			|| type.equals("application/pdf");
		return new ChatAttachmentFile(message.getAttachmentKey(), message.getAttachmentName(), type,
			message.getAttachmentSize(), inline);
	}

	@Override
	public InputStream read(ChatAttachmentFile file, long start, long endInclusive) {
		return storage.read(file.key(), start, endInclusive);
	}

	private User sender(Conversation conversation, Long userId) {
		User sender = conversation.getUserLow().getId().equals(userId) ? conversation.getUserLow()
			: conversation.getUserHigh();
		access.requireMayWrite(sender, conversation.other(userId));
		return sender;
	}

	private ChatMessageResponse publish(Conversation conversation, ChatMessage saved) {
		ChatMessageResponse response = mapper.message(saved);
		events.publishEvent(new ChatMessageSent(response,
			List.of(conversation.getUserLow().getEmail(), conversation.getUserHigh().getEmail())));
		return response;
	}

	private static String extension(String fileName) {
		String ext = StringUtils.getFilenameExtension(fileName);
		return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
	}
}
