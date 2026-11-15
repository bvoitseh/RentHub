package com.bohakka.renthub.user.service;

import com.bohakka.renthub.common.ratelimit.RateLimitProperties;
import com.bohakka.renthub.common.ratelimit.RateLimiter;
import com.bohakka.renthub.user.domain.EmailToken;
import com.bohakka.renthub.user.domain.EmailTokenType;
import com.bohakka.renthub.user.domain.User;
import com.bohakka.renthub.user.event.PasswordResetEmailRequested;
import com.bohakka.renthub.user.repository.UserRepository;
import com.bohakka.renthub.user.security.SessionTerminator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-3a: password reset by a single-use 1-hour link; old sessions are invalidated after the reset. */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	static final String SCOPE = "password-reset";

	private final UserRepository users;
	private final EmailTokenService tokens;
	private final PasswordEncoder passwordEncoder;
	private final RateLimiter rateLimiter;
	private final RateLimitProperties limits;
	private final ApplicationEventPublisher events;
	private final SessionTerminator sessionTerminator;

	/** Always completes silently: the caller never learns whether the e-mail exists (NFR-14). */
	@Transactional
	public void requestReset(String email) {
		String normalized = User.normalizeEmail(email);
		rateLimiter.consume(SCOPE, normalized, limits.passwordReset());
		users.findByEmail(normalized)
			.filter(User::isActive)
			.ifPresent(user -> {
				String rawToken = tokens.issue(user, EmailTokenType.RESET_PASSWORD);
				events.publishEvent(new PasswordResetEmailRequested(user.getEmail(), user.getName(), rawToken));
			});
	}

	@Transactional
	public void reset(String rawToken, String newPassword) {
		EmailToken token = tokens.consume(rawToken, EmailTokenType.RESET_PASSWORD);
		User user = token.getUser();
		user.changePassword(passwordEncoder.encode(newPassword));
		sessionTerminator.expireAllSessions(user.getId());
	}
}
