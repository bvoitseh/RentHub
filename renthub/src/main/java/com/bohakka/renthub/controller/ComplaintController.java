package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.ComplaintRequest;
import com.bohakka.renthub.dto.ComplaintResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/complaints — file a complaint (FR-21). Authenticated. */
@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
class ComplaintController {

	private final ComplaintService complaints;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ComplaintResponse create(@AuthenticationPrincipal AppUserDetails principal, @Valid @RequestBody ComplaintRequest request) {
		return complaints.create(principal.getId(), request);
	}
}
