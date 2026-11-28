package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.ChatUnreadNotice;
import com.bohakka.renthub.properties.ChatProperties;
import com.bohakka.renthub.repository.ChatMessageRepository;
import com.bohakka.renthub.repository.ConversationRepository;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The scheduler actor for chat e-mails (D-57): every minute, a participant with a message from the other side unread
 * for longer than {@code app.chat.notify-after} gets one e-mail; the next one only after they have read up to there.
 * Each conversation row is locked while its marker moves, so a parallel "mark read" cannot be lost.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNotificationJob {

	private final ConversationRepository conversations;
	private final ChatMessageRepository messages;
	private final ChatProperties properties;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Scheduled(cron = "${app.chat.notify-cron}", zone = "${app.time-zone}")
	@Transactional
	public void notifyUnread() {
		List<Object[]> due = conversations.findUnreadToNotify(clock.instant().minus(properties.notifyAfter()));
		for (Object[] row : due) {
			Conversation conversation = conversations.lockById((Long) row[0]).orElse(null);
			Long recipientId = (Long) row[1];
			if (conversation == null || !conversation.mayNotify(recipientId)) {
				continue;
			}
			long lastId = messages.findLastId(conversation.getId());
			long unread = messages.countUnread(conversation.getId(), recipientId, conversation.lastReadId(recipientId));
			User recipient = conversation.getUserLow().getId().equals(recipientId) ? conversation.getUserLow()
				: conversation.getUserHigh();
			if (recipient.isActive() && unread > 0) {
				events.publishEvent(new ChatUnreadNotice(conversation.getId(), recipient.getEmail(), recipient.getName(),
					conversation.other(recipientId).getName(), unread));
			}
			conversation.notified(recipientId, lastId);
		}
		if (!due.isEmpty()) {
			log.info("Chat e-mail check: {} unread conversation side(s)", due.size());
		}
	}
}
