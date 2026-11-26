package com.bohakka.renthub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FR-23 for [from, to]: E1 active listings (a current snapshot of listings created before the end), E2 the share of
 * conflict-free bookings in percent (null without bookings) out of {@code checkedBookings}, E3 the average rating
 * (null without reviews) of {@code reviewCount} reviews, E4 the commission of completed bookings, E5 the cities.
 */
public record StatisticsResponse(LocalDate from, LocalDate to, long activeListings, BigDecimal conflictFreeShare,
		long checkedBookings, BigDecimal averageRating, long reviewCount, BigDecimal totalCommission, long cities) {
}
