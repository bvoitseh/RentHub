package com.bohakka.renthub.dto;

/**
 * FR-4 (D-49): the street and house under a pin the owner moved, and the postal code suggested for it. {@code address}
 * is null when no street is known at that point; the owner then types it.
 */
public record PinAddressResponse(String address, String postalCode) {
}
