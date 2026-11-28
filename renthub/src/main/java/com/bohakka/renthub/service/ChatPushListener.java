package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.TypingResponse;
import com.bohakka.renthub.event.ChatMessageSent;
import com.bohakka.renthub.event.TypingSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
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

	/** D-58: typing is a fleeting signal, forwarded at once (nothing is stored). */
	@EventListener
	public void onTyping(TypingSignal event) {
		messaging.convertAndSendToUser(event.recipient(), "/queue/typing",
			new TypingResponse(event.conversationId(), event.userId()));
	}
}
