package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.EmailToken;
import com.bohakka.renthub.entity.EmailTokenType;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.VerificationEmailRequested;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.properties.RateLimitProperties;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.EmailTokenService;
import com.bohakka.renthub.service.EmailVerificationService;
import com.bohakka.renthub.service.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link EmailVerificationService}. */
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

	static final String SCOPE = "resend-verification";

	private final UserRepository users;
	private final EmailTokenService tokens;
	private final RateLimiter rateLimiter;
	private final RateLimitProperties limits;
	private final ApplicationEventPublisher events;

	@Override
	@Transactional
	public void verify(String rawToken) {
		EmailToken token = tokens.consume(rawToken, EmailTokenType.VERIFY_EMAIL);
		token.getUser().markEmailVerified();
	}

	@Override
	@Transactional
	public void resend(Long userId) {
		User user = users.findById(userId)
			.orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
		if (user.isEmailVerified()) {
			throw new ConflictException("user.already_verified", "user.already_verified");
		}
		rateLimiter.consume(SCOPE, user.getEmail(), limits.resendVerification());
		String rawToken = tokens.issue(user, EmailTokenType.VERIFY_EMAIL);
		events.publishEvent(new VerificationEmailRequested(user.getEmail(), user.getName(), rawToken));
	}
}
