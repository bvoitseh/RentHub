package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import java.time.LocalDate;

/** A short reference to a booking, listed in 409 answers that a booking blocks (FR-3c, FR-5, FR-8). */
public record BookingRef(Long id, Long equipmentId, String equipmentTitle, LocalDate dateFrom, LocalDate dateTo,
		BookingStatus status) {

	public static BookingRef of(Booking b) {
		return new BookingRef(b.getId(), b.getEquipment().getId(), b.getEquipment().getTitle(), b.getDateFrom(),
			b.getDateTo(), b.getStatus());
	}
}
