package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.RegisterRequest;
import com.bohakka.renthub.entity.EmailTokenType;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.VerificationEmailRequested;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.properties.RateLimitProperties;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.EmailTokenService;
import com.bohakka.renthub.service.RateLimiter;
import com.bohakka.renthub.service.RegistrationService;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link RegistrationService}. */
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

	static final String SCOPE = "registration";

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final EmailTokenService tokens;
	private final RateLimiter rateLimiter;
	private final RateLimitProperties limits;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
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
