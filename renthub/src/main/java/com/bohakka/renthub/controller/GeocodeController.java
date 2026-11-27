package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.ListingLocationResponse;
import com.bohakka.renthub.dto.PinAddressResponse;
import com.bohakka.renthub.service.ListingLocationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/v1/geocode?settlementId=&street= — FR-4 pin and postal-code suggestion for the listing form (D-48). Always
 * answers: without a street match the pin is the settlement centre ({@code exact=false}). {@code /reverse} gives the
 * street and postal code under a pin the owner moved (D-49). Logged-in users only.
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

	@GetMapping("/reverse")
	PinAddressResponse reverse(@RequestParam @NotNull Long settlementId,
			@RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
			@RequestParam @DecimalMin("-180") @DecimalMax("180") double lon) {
		return locations.describe(settlementId, lat, lon);
	}
}
