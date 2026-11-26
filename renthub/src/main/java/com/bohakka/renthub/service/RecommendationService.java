package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.RecommendationsResponse;

/** FR-16, FR-17: equipment recommended near the viewer. */
public interface RecommendationService {

	/**
	 * {@code viewerId} is null for guests. Browser coordinates ({@code lat}/{@code lon}) are used only for a signed-in
	 * user with geolocation consent (FR-3) and are never stored.
	 */
	RecommendationsResponse recommend(Long viewerId, Double lat, Double lon);
}
