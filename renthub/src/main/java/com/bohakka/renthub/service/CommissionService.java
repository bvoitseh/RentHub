package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.CommissionResponse;
import java.math.BigDecimal;

/** FR-22: the platform commission rate; callers are ADMIN only (NFR-11). */
public interface CommissionService {

	CommissionResponse current();

	/** A new version in force immediately for new booking requests; existing bookings keep theirs (FR-10, D-46). */
	CommissionResponse change(BigDecimal rate);
}
