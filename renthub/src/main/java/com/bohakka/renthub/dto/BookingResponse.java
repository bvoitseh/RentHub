package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.CancelledBy;
import com.bohakka.renthub.entity.Handover;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A booking as its renter or owner sees it. {@code respondBy} is the FR-15b deadline (NEW requests only); contacts are
 * not here, they come from {@code /bookings/{id}/contact} (FR-3b).
 */
public record BookingResponse(Long id, Item equipment, Party renter, Party owner, int units, LocalDate dateFrom,
		LocalDate dateTo, int days, Handover handover, BigDecimal pricePerDay, BigDecimal totalPrice,
		BigDecimal commission, BookingStatus status, boolean expired, Instant createdAt, Instant decidedAt,
		Instant respondBy, CancelledBy cancelledBy, String cancelReason) {

	public record Item(Long id, String title, String coverUrl) {
	}

	public record Party(Long id, String name) {
	}
}
