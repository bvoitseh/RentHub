package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.AvailabilityBlock;
import java.time.LocalDate;

public record AvailabilityBlockResponse(Long id, LocalDate dateFrom, LocalDate dateTo, String reason) {

	public static AvailabilityBlockResponse from(AvailabilityBlock block) {
		return new AvailabilityBlockResponse(block.getId(), block.getDateFrom(), block.getDateTo(), block.getReason());
	}
}
