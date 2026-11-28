package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.PresenceResponse;
import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.PresenceChanged;
import com.bohakka.renthub.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes a user's new status to everyone they have a conversation with (D-58), on {@code /user/queue/presence};
 * the status is null for a partner when either side hides it. Runs after the commit of a visibility change, or at
 * once for socket connects and disconnects (no transaction).
 */
@Component
@RequiredArgsConstructor
public class PresencePushListener {

	private final ConversationRepository conversations;
	private final PresenceService presence;
	private final SimpMessagingTemplate messaging;

	@TransactionalEventListener(fallbackExecution = true)
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void onChanged(PresenceChanged event) {
		Long userId = event.userId();
		for (Conversation c : conversations.findAllFor(userId)) {
			User me = c.getUserLow().getId().equals(userId) ? c.getUserLow() : c.getUserHigh();
			User partner = c.other(userId);
			boolean visible = me.isShowOnlineStatus() && partner.isShowOnlineStatus();
			messaging.convertAndSendToUser(partner.getEmail(), "/queue/presence", new PresenceResponse(userId,
				visible ? presence.isOnline(userId) : null, visible ? presence.lastSeen(userId) : null));
		}
	}
}
