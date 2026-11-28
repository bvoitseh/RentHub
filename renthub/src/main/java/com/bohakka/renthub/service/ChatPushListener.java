package com.bohakka.renthub.service;

import com.bohakka.renthub.event.ChatMessageSent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes a saved chat message over WebSocket (STOMP) to every open session of both participants after the commit
 * (D-57); the client subscribes to {@code /user/queue/chat}. A user without an open session simply gets nothing.
 */
@Component
@RequiredArgsConstructor
public class ChatPushListener {

	public static final String DESTINATION = "/queue/chat";

	private final SimpMessagingTemplate messaging;

	@TransactionalEventListener
	public void onMessage(ChatMessageSent event) {
		event.recipients().forEach(user -> messaging.convertAndSendToUser(user, DESTINATION, event.message()));
	}
}
