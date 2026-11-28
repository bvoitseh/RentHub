package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Who may read and write in a chat (D-57): only the two participants read a conversation; a message may be written by
 * an active user with a verified e-mail to another active user (not the admin, never oneself).
 */
@Component
@RequiredArgsConstructor
class ChatAccess {

	private final ConversationRepository conversations;

	/** The conversation with both users loaded, if the user takes part in it (404 / 403 otherwise). */
	Conversation forParticipant(Long userId, Long conversationId) {
		Conversation conversation = conversations.findWithUsersById(conversationId)
			.orElseThrow(() -> new NotFoundException("chat.not_found", "chat.not_found"));
		conversation.requireParticipant(userId);
		return conversation;
	}

	/** Throws unless {@code sender} may write to {@code recipient}. */
	void requireMayWrite(User sender, User recipient) {
		if (sender.getRole() != Role.USER || !sender.isActive()) {
			throw new ForbiddenException("chat.forbidden", "chat.forbidden");
		}
		if (!sender.isEmailVerified()) {
			throw new ForbiddenException("chat.email_not_verified", "chat.email_not_verified");
		}
		if (!available(recipient)) {
			throw new ConflictException("chat.recipient_unavailable", "chat.recipient_unavailable");
		}
	}

	/** An active regular user, who can receive messages. */
	static boolean available(User user) {
		return user.getRole() == Role.USER && user.isActive();
	}
}
