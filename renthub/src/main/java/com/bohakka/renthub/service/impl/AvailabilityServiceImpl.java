package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.AvailabilityResponse;
import com.bohakka.renthub.entity.AvailabilityBlock;
import com.bohakka.renthub.entity.AvailabilityCalendar;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingPeriod;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.properties.BookingProperties;
import com.bohakka.renthub.repository.AvailabilityBlockRepository;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.service.AvailabilityService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AvailabilityService}. Units are held by CONFIRMED bookings and by NEW requests whose FR-15b deadline has not
 * passed (D-39), even before the hourly job marks them expired; a block closes the whole listing.
 * The calendar window is limited to the booking horizon, which keeps the per-day computation small (NFR-1).
 */
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

	private final EquipmentRepository equipment;
	private final BookingRepository bookings;
	private final AvailabilityBlockRepository blocks;
	private final BookingProperties properties;
	private final BookingDeadline deadline;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public AvailabilityResponse calendar(Long equipmentId, LocalDate from, LocalDate to, Long viewerId) {
		Equipment listing = equipment.findWithDetailsById(equipmentId)
			.filter(e -> e.isPublic() || viewerId != null && e.isOwnedBy(viewerId))
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
		BookingPeriod window = new BookingPeriod(from, to);
		if (window.days() > properties.horizonDays() + 1) {
			throw new BusinessRuleException("availability.window_too_long", "availability.window_too_long",
				properties.horizonDays() + 1);
		}
		AvailabilityCalendar calendar = calendar(listing, window, null);
		LocalDate today = LocalDate.now(clock);
		List<AvailabilityResponse.Day> days = window.dates()
			.map(d -> new AvailabilityResponse.Day(d, d.isBefore(today) ? 0 : calendar.freeUnits(d),
				calendar.isBlocked(d)))
			.toList();
		return new AvailabilityResponse(listing.getId(), listing.getQuantity(), from, to, days);
	}

	@Override
	public void assertFree(Equipment listing, int units, BookingPeriod period, Long ignoredBookingId) {
		List<LocalDate> lacking = calendar(listing, period, ignoredBookingId).daysLacking(units, period);
		if (!lacking.isEmpty()) {
			throw new ConflictException("booking.unavailable", "booking.unavailable").withProperty("days", lacking);
		}
	}

	private AvailabilityCalendar calendar(Equipment listing, BookingPeriod window, Long ignoredBookingId) {
		List<BookingPeriod> closed = blocks.findOverlapping(listing.getId(), window.from(), window.to()).stream()
			.map(AvailabilityBlock::period).toList();
		List<Booking> holding = bookings.findOverlapping(listing.getId(), BookingStatus.HOLDING_UNITS, window.from(),
				window.to()).stream()
			.filter(b -> !b.getId().equals(ignoredBookingId) && !deadline.hasPassed(b))
			.toList();
		return new AvailabilityCalendar(listing.getQuantity(), closed, holding);
	}
}
