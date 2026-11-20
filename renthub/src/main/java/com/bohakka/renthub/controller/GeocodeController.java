package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.GeocodeResponse;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.service.Geocoder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/geocode?q= — FR-4 address preview for the pin; 404 means "place the pin by hand". Logged-in users only. */
@RestController
@RequestMapping("/api/v1/geocode")
@RequiredArgsConstructor
class GeocodeController {

	private final Geocoder geocoder;

	@GetMapping
	GeocodeResponse geocode(@RequestParam @NotBlank @Size(max = 300) String q) {
		return geocoder.geocode(q)
			.orElseThrow(() -> new NotFoundException("geocode.not_found", "geocode.not_found"));
	}
}
