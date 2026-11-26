package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.Review;
import com.bohakka.renthub.event.BookingNoticeType;
import java.time.Instant;

/**
 * Queues booking e-mails (FR-15a, FR-15b, FR-18); they are sent asynchronously after the current transaction commits
 * and not at all if it rolls back. Call inside the transaction that made the change.
 */
public interface BookingNotifier {

	/** A booking change with a fixed recipient side (every type except the review ones). */
	void notify(Booking booking, BookingNoticeType type);

	/** FR-20: tells one side ({@code toOwner}) that the admin cancelled the booking, with the reason. */
	void adminCancelled(Booking booking, boolean toOwner);

	/** FR-18: both sides are invited to review the completed rental until {@code reviewUntil}. */
	void remindToReview(Booking booking, Instant reviewUntil);

	/** A review was published → its target. */
	void reviewPublished(Review review);

	/** The target replied → the review's author. */
	void reviewReplied(Review review);
}
