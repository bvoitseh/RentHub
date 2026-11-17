package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.service.EmailTokenType;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.PasswordResetEmailRequested;
import com.bohakka.renthub.properties.RateLimitProperties;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.security.SessionTerminator;
import com.bohakka.renthub.service.EmailTokenService;
import com.bohakka.renthub.service.PasswordResetService;
import com.bohakka.renthub.service.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link PasswordResetService}. */
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

	static final String SCOPE = "password-reset";

	private final UserRepository users;
	private final EmailTokenService tokens;
	private final PasswordEncoder passwordEncoder;
	private final RateLimiter rateLimiter;
	private final RateLimitProperties limits;
	private final ApplicationEventPublisher events;
	private final SessionTerminator sessionTerminator;

	@Override
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

	@Override
	@Transactional
	public void reset(String rawToken, String newPassword) {
		User user = tokens.consume(rawToken, EmailTokenType.RESET_PASSWORD);
		user.changePassword(passwordEncoder.encode(newPassword));
		sessionTerminator.expireAllSessions(user.getId());
	}
}
