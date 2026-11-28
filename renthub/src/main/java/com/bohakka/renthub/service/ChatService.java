package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.ConversationResponse;
import java.util.List;

/** Conversations between two users (D-57): opening, listing with unread counts, read markers. */
public interface ChatService {

	/** Finds or creates the conversation with {@code otherUserId}; both must be active users, the caller verified. */
	ConversationResponse open(Long userId, Long otherUserId);

	List<ConversationResponse> list(Long userId);

	ConversationResponse get(Long userId, Long conversationId);

	/** Marks everything in the conversation as read by the user. */
	void markRead(Long userId, Long conversationId);

	long unreadTotal(Long userId);

	/** D-58: tells the other participant that the user is typing (only a participant who may write). */
	void typing(Long userId, Long conversationId);
}
