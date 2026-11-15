package com.bohakka.renthub.user.service;

import com.bohakka.renthub.common.error.ForbiddenException;
import com.bohakka.renthub.common.error.NotFoundException;
import com.bohakka.renthub.user.domain.Role;
import com.bohakka.renthub.user.domain.User;
import com.bohakka.renthub.user.repository.UserRepository;
import com.bohakka.renthub.user.security.SessionEstablisher;
import com.bohakka.renthub.user.security.SessionTerminator;
import com.bohakka.renthub.user.web.ProfileUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The caller's own account: profile (FR-1a), geolocation consent (FR-3), deactivation (FR-3c). */
@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final SessionTerminator sessionTerminator;
	private final SessionEstablisher sessions;
	private final Clock clock;

	@Transactional(readOnly = true)
	public User get(Long userId) {
		return users.findById(userId).orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
	}

	@Transactional
	public User updateProfile(Long userId, ProfileUpdateRequest request) {
		User user = get(userId);
		user.updateProfile(request.name(), request.phone(), request.city());
		if (request.ownerType() != null || user.getRole() == Role.OWNER && user.getOwnerType() == null) {
			user.assignOwnerType(request.ownerType(), request.companyName(), request.edrpou());
		}
		return user;
	}

	@Transactional
	public User setGeoConsent(Long userId, boolean consent) {
		User user = get(userId);
		user.setGeoConsent(consent);
		return user;
	}

	/** FR-3c: password confirmation, no active bookings, then every session of the user is ended. */
	@Transactional
	public void deactivate(Long userId, String password, HttpServletRequest http) {
		User user = get(userId);
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new ForbiddenException("user.wrong_password", "user.wrong_password");
		}
		assertNoActiveBookings(user);
		user.deactivate(clock.instant());
		sessionTerminator.expireAllSessions(userId);
		sessions.terminateCurrent(http);
	}

	/**
	 * FR-3c forbids deactivation while the user has NEW or CONFIRMED bookings with dateTo >= today, as renter or owner.
	 * Bookings do not exist yet; the booking feature replaces this with the real query (see DECISIONS.md D-12).
	 */
	private void assertNoActiveBookings(User user) {
		// Intentionally empty until the availability-booking feature.
	}
}
