package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.BookingReviewsResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.dto.ReviewRequest;
import com.bohakka.renthub.dto.ReviewResponse;

/** Two-sided blind reviews after a completed rental, replies and their visibility (FR-18, FR-19). */
public interface ReviewService {

	/** A side of a COMPLETED booking reviews the other, once, within the window; both get published together. */
	ReviewResponse create(Long authorId, Long bookingId, ReviewRequest request);

	/** The caller's own review, the other side's once published, and whether the caller may still review. */
	BookingReviewsResponse forBooking(Long userId, Long bookingId);

	/** A user's published reviews (public); {@code viewerId} may be null and decides {@code canReply}. */
	PageResponse<ReviewResponse> forUser(Long targetId, Long viewerId, int page);

	/** The target's one reply to a published review, within the reply window. */
	ReviewResponse reply(Long userId, Long reviewId, String text);
}
