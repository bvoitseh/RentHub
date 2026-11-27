package com.bohakka.renthub.entity;

/** FR-4 (D-48): where a listing is — settlement, street and house, optional postal code and the confirmed pin. */
public record ListingAddress(Settlement settlement, String street, String postalCode, GeoPoint point) {
}
