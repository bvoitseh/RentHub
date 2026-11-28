package com.bohakka.renthub.event;

/** A participant has unread chat messages older than the notify delay (D-57); e-mailed after the commit. */
public record ChatUnreadNotice(Long conversationId, String recipientEmail, String recipientName, String senderName,
		long unreadCount) {
}
