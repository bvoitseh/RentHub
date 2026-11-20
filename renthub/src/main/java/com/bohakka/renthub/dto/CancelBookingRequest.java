package com.bohakka.renthub.dto;

import jakarta.validation.constraints.Size;

/** FR-13, FR-13a: the renter may omit the reason; the owner must give 10–500 characters (checked by the booking). */
public record CancelBookingRequest(@Size(max = 500) String reason) {
}
