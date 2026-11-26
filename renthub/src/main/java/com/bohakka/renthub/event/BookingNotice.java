package com.bohakka.renthub.event;

import com.bohakka.renthub.entity.Handover;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A booking e-mail to send after the transaction commits (FR-15a, FR-18). It is a snapshot of everything the text
 * needs, taken inside the transaction, so the asynchronous listener reads no entities. {@code deadline} is the answer
 * deadline (REQUESTED) or the review deadline (REVIEW_REMINDER); {@code ownerPhone} is set for CONFIRMED (FR-3b),
 * {@code cancelReason} for CANCELLED_BY_OWNER (FR-13a), {@code reviewRating}/{@code reviewText} for the review
 * e-mails (the review, or the reply for REVIEW_REPLIED); otherwise they are null.
 */
public record BookingNotice(BookingNoticeType type, Long bookingId, Long recipientId, boolean recipientIsOwner,
		String recipientEmail, String recipientName, Long otherPartyId, String otherPartyName, String equipmentTitle,
		LocalDate dateFrom, LocalDate dateTo, int days, int units, Handover handover, BigDecimal totalPrice,
		Instant deadline, String ownerPhone, String cancelReason, Integer reviewRating, String reviewText) {
}
