package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.RecommendationsResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.RecommendationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/v1/recommendations — equipment nearby for guests and users (FR-16, FR-17). */
@RestController
@RequiredArgsConstructor
class RecommendationController {

	private final RecommendationService recommendations;

	@GetMapping("/api/v1/recommendations")
	RecommendationsResponse recommend(@AuthenticationPrincipal AppUserDetails principal,
			@RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double lat,
			@RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double lon) {
		return recommendations.recommend(principal == null ? null : principal.getId(), lat, lon);
	}
}
