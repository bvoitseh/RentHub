package com.bohakka.renthub.event;

/**
 * The booking e-mails of FR-15a / FR-15b / FR-18. Each type has its templates {@code mail.booking.<key>.subject/body}
 * in messages_uk.properties. Booking-change types go to a fixed side ({@link #toOwner()}); the review types go to the
 * side the review concerns, chosen by the notifier.
 */
public enum BookingNoticeType {
	/** New request → owner. */
	REQUESTED("requested", true),
	/** Request confirmed → renter, with the owner's contact (FR-3b). */
	CONFIRMED("confirmed", false),
	/** Request rejected by the owner → renter. */
	REJECTED("rejected", false),
	/** Request expired without an answer (FR-15b) → renter. */
	EXPIRED("expired", false),
	/** Booking cancelled by the renter → owner. */
	CANCELLED_BY_RENTER("cancelled-by-renter", true),
	/** Booking cancelled by the owner, with the reason (FR-13a) → renter. */
	CANCELLED_BY_OWNER("cancelled-by-owner", false),
	/** FR-20: cancelled because the admin blocked a user or the listing → the other side. */
	CANCELLED_BY_ADMIN("cancelled-by-admin", false),
	/** FR-18: the rental is completed, leave a review within the window → both sides. */
	REVIEW_REMINDER("review-reminder", false),
	/** A review about the recipient was published → its target. */
	REVIEW_PUBLISHED("review-published", false),
	/** The target replied to the recipient's review → its author. */
	REVIEW_REPLIED("review-replied", false);

	private final String key;
	private final boolean toOwner;

	BookingNoticeType(String key, boolean toOwner) {
		this.key = key;
		this.toOwner = toOwner;
	}

	/** Message-key part, e.g. {@code mail.booking.requested.subject}. */
	public String key() {
		return key;
	}

	/** For the booking-change types: whether the e-mail goes to the owner (otherwise the renter). */
	public boolean toOwner() {
		return toOwner;
	}
}
