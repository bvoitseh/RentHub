package com.bohakka.renthub.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** FR-22: the rate in force, when it was set ({@code updatedAt}) and the latest versions, newest first. */
public record CommissionResponse(BigDecimal rate, Instant updatedAt, List<Version> history) {

	public record Version(BigDecimal rate, Instant validFrom) {
	}
}
