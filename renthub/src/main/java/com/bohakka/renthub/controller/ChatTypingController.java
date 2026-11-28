package com.bohakka.renthub.controller;

import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.ChatService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/** STOMP SEND to {@code /app/chat/{id}/typing} (D-58): the only message a client may send over the socket. */
@Controller
@RequiredArgsConstructor
class ChatTypingController {

	private final ChatService chats;

	@MessageMapping("/chat/{conversationId}/typing")
	void typing(@DestinationVariable Long conversationId, Principal principal) {
		if (principal instanceof Authentication auth && auth.getPrincipal() instanceof AppUserDetails user) {
			chats.typing(user.getId(), conversationId);
		}
	}
}
