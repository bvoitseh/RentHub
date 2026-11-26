package com.bohakka.renthub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** FR-22: a category and its season in months 1-12 (from > to wraps over New Year). */
public record CategoryRequest(@NotBlank @Size(min = 2, max = 100) String name, @NotNull @Min(1) @Max(12) Integer seasonFrom,
		@NotNull @Min(1) @Max(12) Integer seasonTo) {
}
