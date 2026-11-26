package com.bohakka.renthub.event;

/**
 * The booking e-mails of FR-15a / FR-15b. Each type has its templates {@code mail.booking.<key>.subject/body} in
 * messages_uk.properties and says which side receives it.
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
	CANCELLED_BY_OWNER("cancelled-by-owner", false);

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

	/** Whether the e-mail goes to the equipment owner (otherwise to the renter). */
	public boolean toOwner() {
		return toOwner;
	}
}
