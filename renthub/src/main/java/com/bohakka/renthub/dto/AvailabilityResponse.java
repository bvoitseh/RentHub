package com.bohakka.renthub.dto;

import java.time.LocalDate;
import java.util.List;

/** FR-9: free units of a listing for each day of {@code [from, to]}; {@code blocked} marks the owner's closed days. */
public record AvailabilityResponse(Long equipmentId, int quantity, LocalDate from, LocalDate to, List<Day> days) {

	public record Day(LocalDate date, int freeUnits, boolean blocked) {
	}
}
