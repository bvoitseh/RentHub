package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.properties.BookingProperties;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * FR-15b: a NEW request expires {@code requestTtl} (48 h) after it was made, or when its first day starts (midnight,
 * Europe/Kyiv), whichever comes first.
 */
@Component
@RequiredArgsConstructor
public class BookingDeadline {

	private final BookingProperties properties;
	private final Clock clock;

	/** The moment the owner must answer by; null once the request is no longer NEW. */
	public Instant respondBy(Booking booking) {
		if (booking.getStatus() != BookingStatus.NEW) {
			return null;
		}
		Instant byTtl = booking.getCreatedAt().plus(properties.requestTtl());
		Instant byStart = booking.getDateFrom().atStartOfDay(clock.getZone()).toInstant();
		return byTtl.isBefore(byStart) ? byTtl : byStart;
	}

	public boolean hasPassed(Booking booking) {
		Instant deadline = respondBy(booking);
		return deadline != null && !clock.instant().isBefore(deadline);
	}
}
