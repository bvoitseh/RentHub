package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-21: a complaint about exactly one booking or one listing, with a description of 10–1000 characters. */
public record ComplaintRequest(Long bookingId, Long equipmentId, @NotBlank @Size(min = 10, max = 1000) String description) {
}
