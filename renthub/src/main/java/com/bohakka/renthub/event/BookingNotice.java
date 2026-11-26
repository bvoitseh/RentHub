package com.bohakka.renthub.event;

import com.bohakka.renthub.entity.Handover;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A booking e-mail to send after the booking transaction commits (FR-15a). It is a snapshot of everything the text
 * needs, taken inside the transaction, so the asynchronous listener reads no entities. {@code deadline} is set for
 * {@link BookingNoticeType#REQUESTED}, {@code ownerPhone} for {@link BookingNoticeType#CONFIRMED} (FR-3b),
 * {@code cancelReason} for {@link BookingNoticeType#CANCELLED_BY_OWNER} (FR-13a); otherwise they are null.
 */
public record BookingNotice(BookingNoticeType type, Long bookingId, String recipientEmail, String recipientName,
		String otherPartyName, String equipmentTitle, LocalDate dateFrom, LocalDate dateTo, int days, int units,
		Handover handover, BigDecimal totalPrice, Instant deadline, String ownerPhone, String cancelReason) {
}
