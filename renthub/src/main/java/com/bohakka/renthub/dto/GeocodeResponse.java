package com.bohakka.renthub.dto;

/** FR-4: address preview for the map pin; {@code city} (city, town or village) may be null (D-46). */
public record GeocodeResponse(double lat, double lon, String displayName, String city) {
}
