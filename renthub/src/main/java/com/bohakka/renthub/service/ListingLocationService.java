package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.ListingLocationResponse;

/** FR-4 (D-48): the pin and postal code suggested for a listing at {@code street} in a settlement. */
public interface ListingLocationService {

	ListingLocationResponse locate(Long settlementId, String street);
}
