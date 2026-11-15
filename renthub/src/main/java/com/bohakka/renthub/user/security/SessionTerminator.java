package com.bohakka.renthub.user.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

/**
 * Expires every active session of a user (FR-3a password reset, FR-3c deactivation, FR-20 admin block).
 * Backed by Spring's in-memory {@link SessionRegistry}; the ConcurrentSessionFilter rejects expired sessions
 * on their next request with a 401 problem detail.
 */
@Component
@RequiredArgsConstructor
public class SessionTerminator {

	private final SessionRegistry sessionRegistry;

	public void expireAllSessions(Long userId) {
		sessionRegistry.getAllPrincipals().stream()
			.filter(principal -> principal instanceof AppUserDetails details && details.getId().equals(userId))
			.flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
			.forEach(SessionInformation::expireNow);
	}
}
