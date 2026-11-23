package com.bohakka.renthub.entity;

import java.time.LocalDate;
import java.util.List;

/**
 * Free units of one listing per day (FR-9): {@code quantity − Σ units} of the bookings holding that day, or 0 on a day
 * covered by an availability block (a block closes every unit). Which bookings hold units is decided by the caller.
 */
public final class AvailabilityCalendar {

	private final int quantity;
	private final List<BookingPeriod> blocks;
	private final List<Booking> bookings;

	public AvailabilityCalendar(int quantity, List<BookingPeriod> blocks, List<Booking> bookings) {
		this.quantity = quantity;
		this.blocks = List.copyOf(blocks);
		this.bookings = List.copyOf(bookings);
	}

	public boolean isBlocked(LocalDate day) {
		return blocks.stream().anyMatch(b -> b.contains(day));
	}

	public int freeUnits(LocalDate day) {
		if (isBlocked(day)) {
			return 0;
		}
		int taken = bookings.stream().filter(b -> b.period().contains(day)).mapToInt(Booking::getUnits).sum();
		return Math.max(0, quantity - taken);
	}

	/** FR-11: the days of {@code period} that do not have {@code units} free units. */
	public List<LocalDate> daysLacking(int units, BookingPeriod period) {
		return period.dates().filter(day -> freeUnits(day) < units).toList();
	}
}
