package com.bohakka.renthub.service;

import com.bohakka.renthub.security.AppUserDetails;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/** Feeds STOMP session connects and disconnects of signed-in users into {@link PresenceService} (D-58). */
@Component
@RequiredArgsConstructor
public class PresenceSocketListener {

	private final PresenceService presence;

	@EventListener
	public void onConnected(SessionConnectedEvent event) {
		Long userId = userId(event.getUser());
		if (userId != null) {
			presence.connected(userId, sessionId(event));
		}
	}

	@EventListener
	public void onDisconnected(SessionDisconnectEvent event) {
		Long userId = userId(event.getUser());
		if (userId != null) {
			presence.disconnected(userId, event.getSessionId());
		}
	}

	private static String sessionId(AbstractSubProtocolEvent event) {
		return SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders());
	}

	private static Long userId(Principal principal) {
		return principal instanceof Authentication auth && auth.getPrincipal() instanceof AppUserDetails details
			? details.getId() : null;
	}
}
