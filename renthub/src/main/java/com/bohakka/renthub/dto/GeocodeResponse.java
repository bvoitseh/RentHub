package com.bohakka.renthub.dto;

/** FR-4: address preview for the map pin. */
public record GeocodeResponse(double lat, double lon, String displayName) {
}
