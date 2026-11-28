package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ConversationResponse;
import com.bohakka.renthub.entity.ChatMessage;
import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.TypingSignal;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.ChatMessageRepository;
import com.bohakka.renthub.repository.ConversationRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.ChatService;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ChatService} (D-57). Opening locks both users in ascending id order (the D-40 lock order), so two parallel
 * "open" calls for the same pair cannot create two conversations; the pair is also unique in the database.
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

	private final ConversationRepository conversations;
	private final ChatMessageRepository messages;
	private final UserRepository users;
	private final ChatAccess access;
	private final ChatMapper mapper;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
	@Transactional
	public ConversationResponse open(Long userId, Long otherUserId) {
		if (userId.equals(otherUserId)) {
			throw new BusinessRuleException("chat.self", "chat.self");
		}
		User first = lockUser(Math.min(userId, otherUserId));
		User second = lockUser(Math.max(userId, otherUserId));
		User me = first.getId().equals(userId) ? first : second;
		User other = me == first ? second : first;
		access.requireMayWrite(me, other);
		Conversation conversation = conversations.findByUserLowIdAndUserHighId(first.getId(), second.getId())
			.orElseGet(() -> conversations.save(Conversation.between(me, other, clock.instant())));
		return describe(conversation, userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ConversationResponse> list(Long userId) {
		List<Conversation> found = conversations.findActiveFor(userId);
		if (found.isEmpty()) {
			return List.of();
		}
		Map<Long, ChatMessage> last = messages.findLastOf(found.stream().map(Conversation::getId).toList()).stream()
			.collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));
		Map<Long, Long> unread = unreadByConversation(userId);
		return found.stream()
			.map(c -> mapper.conversation(c, userId, last.get(c.getId()), unread.getOrDefault(c.getId(), 0L)))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ConversationResponse get(Long userId, Long conversationId) {
		return describe(access.forParticipant(userId, conversationId), userId);
	}

	@Override
	@Transactional
	public void markRead(Long userId, Long conversationId) {
		Conversation conversation = conversations.lockById(conversationId)
			.orElseThrow(() -> new NotFoundException("chat.not_found", "chat.not_found"));
		conversation.markRead(userId, messages.findLastId(conversationId));
	}

	@Override
	@Transactional(readOnly = true)
	public long unreadTotal(Long userId) {
		return unreadByConversation(userId).values().stream().mapToLong(Long::longValue).sum();
	}

	@Override
	@Transactional(readOnly = true)
	public void typing(Long userId, Long conversationId) {
		Conversation conversation = access.forParticipant(userId, conversationId);
		User me = conversation.getUserLow().getId().equals(userId) ? conversation.getUserLow() : conversation.getUserHigh();
		User other = conversation.other(userId);
		access.requireMayWrite(me, other);
		events.publishEvent(new TypingSignal(conversationId, userId, other.getEmail()));
	}

	private ConversationResponse describe(Conversation conversation, Long userId) {
		List<ChatMessage> last = messages.findLastOf(List.of(conversation.getId()));
		long unread = messages.countUnread(conversation.getId(), userId, conversation.lastReadId(userId));
		return mapper.conversation(conversation, userId, last.isEmpty() ? null : last.getFirst(), unread);
	}

	private Map<Long, Long> unreadByConversation(Long userId) {
		Map<Long, Long> result = new HashMap<>();
		for (Object[] row : messages.countUnreadByConversation(userId)) {
			result.put((Long) row[0], (Long) row[1]);
		}
		return result;
	}

	private User lockUser(Long id) {
		return users.lockById(id).orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
	}
}
