package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.ConflictException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Booking lifecycle (FR-14) — the single place that lists which status may follow which. Who may trigger a transition
 * and when (owner, renter, before the start, …) is checked by {@link Booking}'s methods.
 */
public enum BookingStatus {
	NEW,
	CONFIRMED,
	REJECTED,
	CANCELLED,
	COMPLETED;

	/** Statuses that hold units on their days (D-39: a pending request reserves them as well as a confirmed one). */
	public static final Set<BookingStatus> HOLDING_UNITS = EnumSet.of(NEW, CONFIRMED);

	public Set<BookingStatus> next() {
		return switch (this) {
			case NEW -> EnumSet.of(CONFIRMED, REJECTED, CANCELLED);
			case CONFIRMED -> EnumSet.of(CANCELLED, COMPLETED);
			case REJECTED, CANCELLED, COMPLETED -> EnumSet.noneOf(BookingStatus.class);
		};
	}

	/** 409 {@code booking.transition_invalid} unless {@code target} may follow this status. */
	public BookingStatus moveTo(BookingStatus target) {
		if (!next().contains(target)) {
			throw new ConflictException("booking.transition_invalid", "booking.transition_invalid");
		}
		return target;
	}
}
