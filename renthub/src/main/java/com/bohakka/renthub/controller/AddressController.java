package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.RegionResponse;
import com.bohakka.renthub.dto.SettlementResponse;
import com.bohakka.renthub.service.AddressService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/regions and /api/v1/settlements — public autocomplete for registration, profile and listings (D-48). */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
class AddressController {

	private final AddressService addresses;

	@GetMapping("/regions")
	List<RegionResponse> regions() {
		return addresses.regions();
	}

	@GetMapping("/settlements")
	List<SettlementResponse> settlements(@RequestParam @NotNull Long regionId,
			@RequestParam @Size(min = 2, max = 60) String q) {
		return addresses.search(regionId, q);
	}
}
