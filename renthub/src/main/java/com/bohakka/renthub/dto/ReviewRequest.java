package com.bohakka.renthub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** FR-18: a rating 1–5 and a comment of up to 1000 characters. */
public record ReviewRequest(@NotNull @Min(1) @Max(5) Integer rating, @NotBlank @Size(max = 1000) String comment) {
}
