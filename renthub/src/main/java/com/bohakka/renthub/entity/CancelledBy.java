package com.bohakka.renthub.entity;

/** Who cancelled a booking (FR-13, FR-13a, FR-20). Only {@code OWNER} counts towards the owner-cancellation counter. */
public enum CancelledBy {
	RENTER,
	OWNER,
	ADMIN
}
