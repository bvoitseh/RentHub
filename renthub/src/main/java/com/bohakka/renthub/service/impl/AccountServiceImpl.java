package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.BookingRef;
import com.bohakka.renthub.dto.ProfileUpdateRequest;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.security.SessionEstablisher;
import com.bohakka.renthub.security.SessionTerminator;
import com.bohakka.renthub.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link AccountService}. */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final UserRepository users;
	private final BookingRepository bookings;
	private final PasswordEncoder passwordEncoder;
	private final SessionTerminator sessionTerminator;
	private final SessionEstablisher sessions;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public User get(Long userId) {
		return users.findById(userId).orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
	}

	@Override
	@Transactional
	public User updateProfile(Long userId, ProfileUpdateRequest request) {
		User user = get(userId);
		user.updateProfile(request.name(), request.phone(), request.city());
		if (request.ownerType() != null) {
			user.assignOwnerType(request.ownerType(), request.companyName(), request.edrpou());
		}
		return user;
	}

	@Override
	@Transactional
	public User setGeoConsent(Long userId, boolean consent) {
		User user = get(userId);
		user.setGeoConsent(consent);
		return user;
	}

	@Override
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
	 * FR-3c forbids deactivation while the user has NEW or CONFIRMED bookings with dateTo >= today, as renter or owner;
	 * the 409 lists them.
	 */
	private void assertNoActiveBookings(User user) {
		List<Booking> active = bookings.findActiveForUser(user.getId(), BookingStatus.HOLDING_UNITS, LocalDate.now(clock));
		if (!active.isEmpty()) {
			throw new ConflictException("user.has_active_bookings", "user.has_active_bookings")
				.withProperty("bookings", active.stream().map(BookingRef::of).toList());
		}
	}
}
