package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.ChatAttachmentFile;
import com.bohakka.renthub.dto.ChatMessageResponse;
import com.bohakka.renthub.entity.MessageKind;
import java.io.InputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Messages of a conversation (D-57): history, text and attachment messages, attachment access. */
public interface ChatMessageService {

	/** Up to {@code size} messages older than {@code beforeId} (null = the newest), oldest first. */
	List<ChatMessageResponse> history(Long userId, Long conversationId, Long beforeId, int size);

	ChatMessageResponse sendText(Long userId, Long conversationId, String body);

	ChatMessageResponse sendAttachment(Long userId, Long conversationId, MessageKind kind, MultipartFile file);

	/** The attachment of a message, after checking that the user takes part in its conversation. */
	ChatAttachmentFile attachment(Long userId, Long messageId);

	/** Bytes {@code start}…{@code endInclusive} of an attachment returned by {@link #attachment}. */
	InputStream read(ChatAttachmentFile file, long start, long endInclusive);
}
