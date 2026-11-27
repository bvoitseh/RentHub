package com.bohakka.renthub.dto;

/** A Nominatim reverse answer: «street, house» (house may be missing) and a single five-digit postal code or null. */
public record ReverseGeocodeResponse(String street, String postalCode) {
}
