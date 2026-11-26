package com.bohakka.renthub.dto;

import java.time.Instant;

/**
 * The reviews of one booking for one of its sides: {@code mine} (always visible to its author), {@code theirs} (only
 * once published), whether the caller may still review, and until when (completion + window, FR-18).
 */
public record BookingReviewsResponse(ReviewResponse mine, ReviewResponse theirs, boolean canReview,
		Instant reviewUntil) {
}
