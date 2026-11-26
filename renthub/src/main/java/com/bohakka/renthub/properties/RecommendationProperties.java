package com.bohakka.renthub.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * FR-16, FR-17, bound from {@code app.recommendations.*}: the start city used for guests and as a fallback (a new
 * city is configuration, not code, NFR-9), the search radius and how many listings to return.
 */
@Validated
@ConfigurationProperties(prefix = "app.recommendations")
public record RecommendationProperties(@NotBlank String startCity, @DecimalMin("-90") @DecimalMax("90") double startLat,
		@DecimalMin("-180") @DecimalMax("180") double startLon, @DecimalMin("0.1") double radiusKm,
		@Min(1) @Max(48) int limit) {
}
