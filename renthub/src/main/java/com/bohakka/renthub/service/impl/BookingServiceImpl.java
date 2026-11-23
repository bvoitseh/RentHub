package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.BookingRequest;
import com.bohakka.renthub.dto.BookingResponse;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingPeriod;
import com.bohakka.renthub.entity.CommissionRate;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.BookingNoticeType;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.properties.BookingProperties;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.CommissionRateRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.AvailabilityService;
import com.bohakka.renthub.service.BookingNotifier;
import com.bohakka.renthub.service.BookingService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link BookingService}. Creating and confirming a booking run in one transaction that first locks the listing row and
 * then re-checks the free units (FR-11), so parallel requests for the same days are serialised and the excess ones get
 * 409. Pending requests hold their units too (D-39). Row locks are always taken in the order listing → users
 * (ascending id) → booking, so concurrent use cases cannot deadlock or overwrite each other (D-40).
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookings;
	private final EquipmentRepository equipment;
	private final UserRepository users;
	private final CommissionRateRepository rates;
	private final AvailabilityService availability;
	private final BookingDeadline deadline;
	private final BookingMapper mapper;
	private final BookingNotifier notifier;
	private final BookingProperties properties;
	private final Clock clock;

	/** Locks the listing, then renter and owner by ascending id (FR-3c), so every check below reads current rows. */
	@Override
	@Transactional
	public BookingResponse create(Long renterId, BookingRequest request) {
		Equipment listing = equipment.lockById(request.equipmentId())
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
		Long ownerId = listing.getOwner().getId();
		User renter = null;
		for (Long userId : new TreeSet<>(List.of(renterId, ownerId))) {
			User locked = users.lockById(userId)
				.orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
			if (userId.equals(renterId)) {
				renter = locked;
			}
		}
		assertMayBook(renter);
		if (!listing.isPublic()) {
			throw new NotFoundException("equipment.not_found", "equipment.not_found");
		}
		if (listing.isOwnedBy(renterId)) {
			throw rule("booking.own_equipment");
		}
		BookingPeriod period = new BookingPeriod(request.dateFrom(), request.dateTo());
		assertTerms(listing, period, request);
		availability.assertFree(listing, request.units(), period, null);
		CommissionRate rate = rates.findFirstByValidFromLessThanEqualOrderByValidFromDesc(clock.instant())
			.orElseThrow(() -> new IllegalStateException("No commission rate in force"));
		Booking booking = new Booking(listing, renter, request.units(), period, request.handover(), rate,
			clock.instant());
		bookings.save(booking);
		notifier.notify(booking, BookingNoticeType.REQUESTED);
		return mapper.toResponse(booking);
	}

	/** Lock order: listing first (fresh, so {@code isPublic()} is current), then the booking row (FR-11, FR-14). */
	@Override
	@Transactional
	public BookingResponse confirm(Long ownerId, Long bookingId) {
		Long equipmentId = bookings.findEquipmentIdById(bookingId).orElseThrow(BookingServiceImpl::notFound);
		Equipment listing = equipment.lockById(equipmentId).orElseThrow(BookingServiceImpl::notFound);
		Booking booking = bookings.lockById(bookingId).orElseThrow(BookingServiceImpl::notFound);
		if (!booking.isOwner(ownerId)) {
			throw new ForbiddenException("booking.not_participant", "booking.not_participant");
		}
		if (deadline.hasPassed(booking)) {
			throw new ConflictException("booking.expired", "booking.expired");
		}
		if (!listing.isPublic()) {
			throw rule("booking.equipment_unavailable");
		}
		availability.assertFree(listing, booking.getUnits(), booking.period(), booking.getId());
		booking.confirm(ownerId, clock.instant());
		notifier.notify(booking, BookingNoticeType.CONFIRMED);
		return mapper.toResponse(booking);
	}

	/** FR-12; a request whose FR-15b deadline has passed is recorded as expired, not as the owner's rejection. */
	@Override
	@Transactional
	public BookingResponse reject(Long ownerId, Long bookingId) {
		Booking booking = bookings.lockById(bookingId).orElseThrow(BookingServiceImpl::notFound);
		if (booking.isOwner(ownerId) && deadline.hasPassed(booking)) {
			booking.expire(clock.instant());
			notifier.notify(booking, BookingNoticeType.EXPIRED);
		} else {
			booking.reject(ownerId, clock.instant());
			notifier.notify(booking, BookingNoticeType.REJECTED);
		}
		return mapper.toResponse(booking);
	}

	@Override
	@Transactional
	public BookingResponse cancel(Long userId, Long bookingId, String reason) {
		Booking booking = bookings.lockById(bookingId).orElseThrow(BookingServiceImpl::notFound);
		LocalDate today = LocalDate.now(clock);
		if (booking.isRenter(userId)) {
			booking.cancelByRenter(userId, today, clock.instant());
			notifier.notify(booking, BookingNoticeType.CANCELLED_BY_RENTER);
		} else {
			booking.cancelByOwner(userId, reason, today, clock.instant());
			notifier.notify(booking, BookingNoticeType.CANCELLED_BY_OWNER);
		}
		return mapper.toResponse(booking);
	}

	/** FR-1: an active user account with a verified e-mail; the administrator does not rent. */
	private static void assertMayBook(User user) {
		if (user.getRole() != Role.USER || !user.isActive()) {
			throw new ForbiddenException("booking.forbidden", "booking.forbidden");
		}
		if (!user.isEmailVerified()) {
			throw new ForbiddenException("booking.email_not_verified", "booking.email_not_verified");
		}
	}

	/** Booking validation (spec §2): dates, horizon (D-38), min/max days, units, handover. */
	private void assertTerms(Equipment listing, BookingPeriod period, BookingRequest request) {
		LocalDate today = LocalDate.now(clock);
		LocalDate earliest = today.plusDays(properties.minLeadDays());
		if (period.from().isBefore(earliest)) {
			throw rule("booking.too_early", earliest);
		}
		if (period.to().isAfter(today.plusDays(properties.horizonDays()))) {
			throw rule("booking.too_far", properties.horizonDays());
		}
		if (!listing.terms().allowsDuration(period.days())) {
			throw listing.getMaxDays() == null ? rule("booking.too_short", listing.getMinDays())
				: rule("booking.duration_invalid", listing.getMinDays(), listing.getMaxDays());
		}
		if (request.units() > listing.getQuantity()) {
			throw rule("booking.units_invalid", listing.getQuantity());
		}
		if (!request.handover().offeredBy(listing.terms())) {
			throw rule("booking.handover_unavailable");
		}
	}

	private static NotFoundException notFound() {
		return new NotFoundException("booking.not_found", "booking.not_found");
	}

	private static BusinessRuleException rule(String key, Object... args) {
		return new BusinessRuleException(key, key, args);
	}
}
