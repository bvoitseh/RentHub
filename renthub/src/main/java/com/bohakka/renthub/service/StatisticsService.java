package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.StatisticsResponse;
import java.time.LocalDate;

/** FR-23: platform statistics E1-E5 for a period; callers are ADMIN only (NFR-11). */
public interface StatisticsService {

	StatisticsResponse forPeriod(LocalDate from, LocalDate to);
}
