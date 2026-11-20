package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.AvailabilityResponse;
import com.bohakka.renthub.entity.BookingPeriod;
import com.bohakka.renthub.entity.Equipment;
import java.time.LocalDate;

/** Day-by-day availability of a listing (FR-9) and the overbooking check (FR-11). */
public interface AvailabilityService {

	/** Free units per day of {@code [from, to]} for a public listing (or for its owner, whatever its status). */
	AvailabilityResponse calendar(Long equipmentId, LocalDate from, LocalDate to, Long viewerId);

	/**
	 * 409 {@code booking.unavailable} with the days lacking units, unless {@code units} are free on every day of
	 * {@code period}. {@code ignoredBookingId} is the booking being confirmed (it must not count against itself). The
	 * caller holds the listing's row lock (FR-11).
	 */
	void assertFree(Equipment listing, int units, BookingPeriod period, Long ignoredBookingId);
}
