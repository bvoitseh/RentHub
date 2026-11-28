package com.bohakka.renthub.dto;

import java.time.Instant;

/** A conversation as its participant sees it (D-57): the other side, the newest message and the unread count. */
public record ConversationResponse(Long id, ChatPartyResponse other, ChatMessageResponse lastMessage, long unreadCount,
		Instant lastMessageAt) {
}
