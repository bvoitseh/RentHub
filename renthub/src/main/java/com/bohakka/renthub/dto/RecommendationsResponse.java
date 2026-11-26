package com.bohakka.renthub.dto;

import java.util.List;

/**
 * FR-16, FR-17: where the recommendations are near ({@code source} = BROWSER for the user's own coordinates, CITY for
 * the profile city, START_CITY otherwise; {@code label} is the city name, null for BROWSER) and the listings, nearest
 * first, each flagged when its category is in season.
 */
public record RecommendationsResponse(Origin origin, List<Item> items) {

	public enum Source { BROWSER, CITY, START_CITY }

	public record Origin(Source source, String label) {
	}

	public record Item(EquipmentSummaryResponse equipment, boolean inSeason) {
	}
}
