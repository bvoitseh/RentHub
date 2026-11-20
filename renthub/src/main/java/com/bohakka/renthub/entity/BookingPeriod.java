package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/** A rental or block period {@code [from, to]} with {@code to} inclusive (spec §1 conventions). */
public record BookingPeriod(LocalDate from, LocalDate to) {

	public BookingPeriod {
		if (from == null || to == null || to.isBefore(from)) {
			throw new BusinessRuleException("booking.dates_invalid", "booking.dates_invalid");
		}
	}

	/** FR-10: days = dateTo − dateFrom + 1. */
	public int days() {
		return Math.toIntExact(ChronoUnit.DAYS.between(from, to) + 1);
	}

	/** FR-9 overlap rule: {@code a.from ≤ b.to && b.from ≤ a.to}. */
	public boolean overlaps(BookingPeriod other) {
		return !from.isAfter(other.to) && !other.from.isAfter(to);
	}

	public boolean contains(LocalDate day) {
		return !day.isBefore(from) && !day.isAfter(to);
	}

	public Stream<LocalDate> dates() {
		return from.datesUntil(to.plusDays(1));
	}
}
