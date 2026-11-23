package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.Handover;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** FR-10: a booking request for {@code units} units of a listing over {@code [dateFrom, dateTo]}. */
public record BookingRequest(@NotNull Long equipmentId, @NotNull @Min(1) @Max(1000) Integer units,
		@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, @NotNull Handover handover) {
}
