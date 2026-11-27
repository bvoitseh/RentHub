package com.bohakka.renthub.dto;

/**
 * FR-4 (D-48): the pin for a listing address and the suggested postal code. {@code exact} is false when the street
 * was not found and the pin is the settlement centre, which the owner should then correct by hand.
 */
public record ListingLocationResponse(double lat, double lon, String displayName, String postalCode, boolean exact) {
}
