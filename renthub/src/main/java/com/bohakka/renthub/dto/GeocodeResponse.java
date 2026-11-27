package com.bohakka.renthub.dto;

/** A Nominatim answer: the point, its description and the postal code when it is a single five-digit one. */
public record GeocodeResponse(double lat, double lon, String displayName, String postalCode) {
}
