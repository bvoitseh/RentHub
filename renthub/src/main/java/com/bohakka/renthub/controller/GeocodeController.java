package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.ListingLocationResponse;
import com.bohakka.renthub.service.ListingLocationService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/v1/geocode?settlementId=&street= — FR-4 pin and postal-code suggestion for the listing form (D-48). Always
 * answers: without a street match the pin is the settlement centre ({@code exact=false}). Logged-in users only.
 */
@RestController
@RequestMapping("/api/v1/geocode")
@RequiredArgsConstructor
class GeocodeController {

	private final ListingLocationService locations;

	@GetMapping
	ListingLocationResponse geocode(@RequestParam @NotNull Long settlementId,
			@RequestParam(defaultValue = "") @Size(max = 300) String street) {
		return locations.locate(settlementId, street);
	}
}
