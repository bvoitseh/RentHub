package com.bohakka.renthub.service;

import com.bohakka.renthub.properties.RateLimitProperties;
import com.bohakka.renthub.dto.LoginRequest;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.AuthenticationFailedException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.security.SessionEstablisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-2 login and FR-3c restore. Credentials are verified first (with NFR-14 limits per IP and per e-mail);
 * only then is the account state checked, so a wrong password never reveals whether an account is blocked
 * or deactivated.
 */
@Service
@RequiredArgsConstructor
public class LoginService {

	static final String SCOPE = "login";

	private final AuthenticationManager authenticationManager;
	private final UserRepository users;
	private final RateLimiter rateLimiter;
	private final RateLimitProperties limits;
	private final SessionEstablisher sessions;

	@Transactional(readOnly = true)
	public User login(LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
		AppUserDetails principal = authenticate(request, http.getRemoteAddr());
		if (principal.isBlocked()) {
			throw new ForbiddenException("auth.blocked", "auth.blocked");
		}
		if (principal.isDeactivated()) {
			throw new ConflictException("auth.deactivated", "auth.deactivated");
		}
		return startSession(users.findById(principal.getId()).orElseThrow(), http, response);
	}

	/** FR-3c: a deactivated (not blocked) user with valid credentials restores the account and is logged in. */
	@Transactional
	public User restore(LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
		AppUserDetails principal = authenticate(request, http.getRemoteAddr());
		if (principal.isBlocked()) {
			throw new ForbiddenException("auth.blocked", "auth.blocked");
		}
		if (!principal.isDeactivated()) {
			throw new ConflictException("auth.not_deactivated", "auth.not_deactivated");
		}
		User user = users.findById(principal.getId()).orElseThrow();
		user.restore();
		return startSession(user, http, response);
	}

	/** Opens a session for an already verified account (login, restore, auto-login after registration). */
	public User startSession(User user, HttpServletRequest http, HttpServletResponse response) {
		rateLimiter.clear(SCOPE, user.getEmail());
		AppUserDetails principal = new AppUserDetails(user);
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null,
			principal.getAuthorities());
		sessions.establish(authentication, http, response);
		return user;
	}

	private AppUserDetails authenticate(LoginRequest request, String clientIp) {
		String email = User.normalizeEmail(request.email());
		rateLimiter.assertAllowed(SCOPE, clientIp);
		rateLimiter.assertAllowed(SCOPE, email);
		try {
			Authentication result = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
			return (AppUserDetails) result.getPrincipal();
		} catch (AuthenticationException ex) {
			rateLimiter.record(SCOPE, clientIp, limits.login());
			rateLimiter.record(SCOPE, email, limits.login());
			throw new AuthenticationFailedException("auth.invalid_credentials", "auth.invalid_credentials");
		}
	}
}
