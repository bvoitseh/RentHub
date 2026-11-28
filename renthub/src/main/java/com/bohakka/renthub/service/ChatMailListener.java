package com.bohakka.renthub.service;

import com.bohakka.renthub.RenthubApplication;
import com.bohakka.renthub.dto.EmailMessage;
import com.bohakka.renthub.event.ChatUnreadNotice;
import com.bohakka.renthub.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * E-mails a participant about unread chat messages (D-57) after the job's transaction commits, on the async pool.
 * Texts are {@code mail.chat.unread.subject/body}: {0} recipient, {1} sender, {2} unread count, {3} conversation link.
 */
@Component
@RequiredArgsConstructor
public class ChatMailListener {

	private final MailDelivery delivery;
	private final MessageSource messages;
	private final AppProperties app;

	@Async
	@TransactionalEventListener
	public void onUnread(ChatUnreadNotice notice) {
		Object[] args = {notice.recipientName(), notice.senderName(), String.valueOf(notice.unreadCount()),
			app.baseUrl() + "/messages/" + notice.conversationId()};
		EmailMessage message = new EmailMessage(notice.recipientEmail(),
			messages.getMessage("mail.chat.unread.subject", args, RenthubApplication.UI_LOCALE),
			messages.getMessage("mail.chat.unread.body", args, RenthubApplication.UI_LOCALE));
		delivery.deliver(message, "chat " + notice.conversationId() + " unread");
	}
}
