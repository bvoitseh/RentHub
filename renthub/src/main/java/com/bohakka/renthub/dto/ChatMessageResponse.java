package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.MessageKind;
import java.time.Instant;

/** One chat message (D-57); {@code body} is set for TEXT, {@code attachment} for FILE, VOICE and VIDEO. */
public record ChatMessageResponse(Long id, Long conversationId, Long senderId, MessageKind kind, String body,
		ChatAttachmentResponse attachment, Instant createdAt) {
}
