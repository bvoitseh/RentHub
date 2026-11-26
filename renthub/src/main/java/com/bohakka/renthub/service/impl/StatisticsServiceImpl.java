package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.StatisticsResponse;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.repository.ReviewRepository;
import com.bohakka.renthub.service.StatisticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link StatisticsService} (FR-23, D-46). The period is whole days in Europe/Kyiv, both ends included; timestamps
 * count from the start of {@code from} to the start of the day after {@code to}.
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final EquipmentRepository equipment;
	private final BookingRepository bookings;
	private final ReviewRepository reviews;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public StatisticsResponse forPeriod(LocalDate from, LocalDate to) {
		if (from == null || to == null || to.isBefore(from)) {
			throw new BusinessRuleException("statistics.period_invalid", "statistics.period_invalid");
		}
		Instant start = from.atStartOfDay(clock.getZone()).toInstant();
		Instant end = to.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

		long checked = bookings.countHeldOverlapping(from, to);
		BigDecimal share = checked == 0 ? null
			: BigDecimal.valueOf(checked - bookings.countHeldOverlappingWithConflict(from, to)).multiply(HUNDRED)
				.divide(BigDecimal.valueOf(checked), 1, RoundingMode.HALF_UP);
		Object[] rating = reviews.ratingPublishedBetween(start, end).getFirst();
		BigDecimal average = rating[0] == null ? null
			: BigDecimal.valueOf(((Number) rating[0]).doubleValue()).setScale(2, RoundingMode.HALF_UP);

		return new StatisticsResponse(from, to, equipment.countActiveCreatedBefore(end), share, checked, average,
			((Number) rating[1]).longValue(), bookings.sumCommissionCompleted(start, end),
			equipment.countCitiesActiveCreatedBefore(end));
	}
}
