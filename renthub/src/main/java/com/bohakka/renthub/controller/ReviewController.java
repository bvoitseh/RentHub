package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.BookingReviewsResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.dto.ReplyRequest;
import com.bohakka.renthub.dto.ReviewRequest;
import com.bohakka.renthub.dto.ReviewResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reviews (FR-18, FR-19): {@code /bookings/{id}/reviews} for the booking's sides, {@code /users/{id}/reviews}
 * (published, public) and {@code /reviews/{id}/reply} for the review's target.
 */
@RestController
@RequiredArgsConstructor
class ReviewController {

	private final ReviewService reviews;

	@PostMapping("/api/v1/bookings/{id:\\d+}/reviews")
	@ResponseStatus(HttpStatus.CREATED)
	ReviewResponse create(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@Valid @RequestBody ReviewRequest request) {
		return reviews.create(principal.getId(), id, request);
	}

	@GetMapping("/api/v1/bookings/{id:\\d+}/reviews")
	BookingReviewsResponse forBooking(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return reviews.forBooking(principal.getId(), id);
	}

	@GetMapping("/api/v1/users/{id:\\d+}/reviews")
	PageResponse<ReviewResponse> forUser(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal,
			@RequestParam(defaultValue = "0") @Min(0) int page) {
		return reviews.forUser(id, principal == null ? null : principal.getId(), page);
	}

	@PostMapping("/api/v1/reviews/{id:\\d+}/reply")
	ReviewResponse reply(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@Valid @RequestBody ReplyRequest request) {
		return reviews.reply(principal.getId(), id, request.text());
	}
}
