package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.BookingRequest;
import com.bohakka.renthub.dto.BookingResponse;
import com.bohakka.renthub.dto.CancelBookingRequest;
import com.bohakka.renthub.dto.ContactResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.BookingQueryService;
import com.bohakka.renthub.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/bookings — requests (FR-10), decisions (FR-12…FR-13a), lists and contacts (FR-3b). Authenticated only. */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
class BookingController {

	private final BookingService bookings;
	private final BookingQueryService queries;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	BookingResponse create(@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody BookingRequest request) {
		return bookings.create(principal.getId(), request);
	}

	@GetMapping("/my")
	PageResponse<BookingResponse> mine(@AuthenticationPrincipal AppUserDetails principal,
			@RequestParam(defaultValue = "0") @Min(0) int page) {
		return queries.mine(principal.getId(), page);
	}

	@GetMapping("/incoming")
	PageResponse<BookingResponse> incoming(@AuthenticationPrincipal AppUserDetails principal,
			@RequestParam(required = false) Set<BookingStatus> status,
			@RequestParam(defaultValue = "0") @Min(0) int page) {
		return queries.incoming(principal.getId(), status, page);
	}

	@PostMapping("/{id:\\d+}/confirm")
	BookingResponse confirm(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return bookings.confirm(principal.getId(), id);
	}

	@PostMapping("/{id:\\d+}/reject")
	BookingResponse reject(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return bookings.reject(principal.getId(), id);
	}

	@PostMapping("/{id:\\d+}/cancel")
	BookingResponse cancel(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@Valid @RequestBody(required = false) CancelBookingRequest request) {
		return bookings.cancel(principal.getId(), id, request == null ? null : request.reason());
	}

	@GetMapping("/{id:\\d+}/contact")
	ContactResponse contact(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return queries.contact(principal.getId(), id);
	}
}
