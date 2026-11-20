package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.CatalogQuery;
import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentRequest;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.CatalogService;
import com.bohakka.renthub.service.ListingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/equipment — public catalogue and card (FR-6, FR-7), the owner's listing management (FR-4, FR-5). */
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
class EquipmentController {

	private final CatalogService catalog;
	private final ListingService listings;

	@GetMapping
	PageResponse<EquipmentSummaryResponse> search(@Valid @ModelAttribute CatalogQuery query) {
		return catalog.search(query);
	}

	@GetMapping("/{id:\\d+}")
	EquipmentDetailsResponse card(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
		return catalog.card(id, principal == null ? null : principal.getId());
	}

	@GetMapping("/my")
	List<EquipmentSummaryResponse> mine(@AuthenticationPrincipal AppUserDetails principal) {
		return listings.listMine(principal.getId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	EquipmentDetailsResponse create(@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody EquipmentRequest request) {
		return listings.create(principal.getId(), request);
	}

	@PutMapping("/{id:\\d+}")
	EquipmentDetailsResponse update(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@Valid @RequestBody EquipmentRequest request) {
		return listings.update(principal.getId(), id, request);
	}

	@PostMapping("/{id:\\d+}/archive")
	EquipmentDetailsResponse archive(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return listings.archive(principal.getId(), id);
	}

	@DeleteMapping("/{id:\\d+}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		listings.delete(principal.getId(), id);
	}
}
