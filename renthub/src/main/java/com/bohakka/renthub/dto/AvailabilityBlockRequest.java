package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** FR-8: close {@code [dateFrom, dateTo]} for rental. */
public record AvailabilityBlockRequest(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo,
		@Size(max = 200) String reason) {
}
