package com.bohakka.renthub.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * FR-4, NFR-13: Nominatim endpoint, the identifying User-Agent its usage policy requires, and the countries
 * searched (ISO codes, comma-separated).
 */
@Validated
@ConfigurationProperties(prefix = "app.geocoding")
public record GeocodingProperties(@NotBlank String url, @NotBlank String userAgent, @NotBlank String countryCodes) {
}
