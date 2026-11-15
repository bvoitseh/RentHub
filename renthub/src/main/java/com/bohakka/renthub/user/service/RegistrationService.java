package com.bohakka.renthub.user.service;

import com.bohakka.renthub.common.error.BusinessRuleException;
import com.bohakka.renthub.common.error.ConflictException;
import com.bohakka.renthub.common.ratelimit.RateLimitProperties;
import com.bohakka.renthub.common.ratelimit.RateLimiter;
import com.bohakka.renthub.user.domain.EmailTokenType;
import com.bohakka.renthub.user.domain.Role;
import com.bohakka.renthub.user.domain.User;
import com.bohakka.renthub.user.event.VerificationEmailRequested;
import com.bohakka.renthub.user.repository.UserRepository;
import com.bohakka.renthub.user.web.RegisterRequest;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-1 / FR-1a: creates a renter or owner account and triggers the verification e-mail. */
@Service
@RequiredArgsConstructor
public class RegistrationService {

	static final String SCOPE = "registration";

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final EmailTokenService tokens;
	private final RateLimiter rateLimiter;
	private final RateLimitProperties limits;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Transactional
	public User register(RegisterRequest request, String clientIp) {
		rateLimiter.consume(SCOPE, clientIp, limits.registration());
		if (request.role() == Role.ADMIN) {
			throw new BusinessRuleException("user.role_not_allowed", "user.role_not_allowed");
		}
		String email = User.normalizeEmail(request.email());
		if (users.existsByEmail(email)) {
			throw new ConflictException("user.email_taken", "user.email_taken");
		}
		User user = new User(request.name(), email, request.phone(), passwordEncoder.encode(request.password()),
			request.role(), request.city(), clock.instant());
		if (user.getRole() == Role.OWNER) {
			if (request.ownerType() == null) {
				throw new BusinessRuleException("user.owner_type_required", "user.owner_type_required");
			}
			user.assignOwnerType(request.ownerType(), request.companyName(), request.edrpou());
		}
		users.save(user);
		String rawToken = tokens.issue(user, EmailTokenType.VERIFY_EMAIL);
		events.publishEvent(new VerificationEmailRequested(user.getEmail(), user.getName(), rawToken));
		return user;
	}
}
