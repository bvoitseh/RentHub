package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.AvailabilityBlockRequest;
import com.bohakka.renthub.dto.AvailabilityBlockResponse;
import com.bohakka.renthub.dto.AvailabilityResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.AvailabilityBlockService;
import com.bohakka.renthub.service.AvailabilityService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/equipment/{id}/availability (public, FR-9) and /blocks (the owner's calendar, FR-8). */
@RestController
@RequestMapping("/api/v1/equipment/{id:\\d+}")
@RequiredArgsConstructor
class AvailabilityController {

	private final AvailabilityService availability;
	private final AvailabilityBlockService blocks;

	@GetMapping("/availability")
	AvailabilityResponse availability(@PathVariable Long id, @RequestParam LocalDate from,
			@RequestParam LocalDate to, @AuthenticationPrincipal AppUserDetails principal) {
		return availability.calendar(id, from, to, principal == null ? null : principal.getId());
	}

	@GetMapping("/blocks")
	List<AvailabilityBlockResponse> blocks(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return blocks.list(principal.getId(), id);
	}

	@PostMapping("/blocks")
	@ResponseStatus(HttpStatus.CREATED)
	AvailabilityBlockResponse block(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@Valid @RequestBody AvailabilityBlockRequest request) {
		return blocks.create(principal.getId(), id, request);
	}

	@DeleteMapping("/blocks/{blockId:\\d+}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void unblock(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@PathVariable Long blockId) {
		blocks.delete(principal.getId(), id, blockId);
	}
}
