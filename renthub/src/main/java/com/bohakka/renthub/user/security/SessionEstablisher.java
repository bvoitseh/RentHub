package com.bohakka.renthub.user.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Creates the server-side session after a successful REST login (Spring Security reference pattern for
 * controller-based authentication): rotates the session id (fixation protection), stores the security context
 * in the session and registers the session so it can be expired later ({@link SessionTerminator}).
 */
@Component
@RequiredArgsConstructor
public class SessionEstablisher {

	private final SecurityContextHolderStrategy contextHolderStrategy;
	private final SecurityContextRepository contextRepository;
	private final SessionRegistry sessionRegistry;

	public void establish(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
		if (request.getSession(false) != null) {
			request.changeSessionId();
		}
		SecurityContext context = contextHolderStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		contextHolderStrategy.setContext(context);
		contextRepository.saveContext(context, request, response);
		sessionRegistry.registerNewSession(request.getSession().getId(), authentication.getPrincipal());
	}

	/** Ends the current session and clears the security context (used by deactivation). */
	public void terminateCurrent(HttpServletRequest request) {
		var session = request.getSession(false);
		if (session != null) {
			sessionRegistry.removeSessionInformation(session.getId());
			session.invalidate();
		}
		contextHolderStrategy.clearContext();
	}
}
