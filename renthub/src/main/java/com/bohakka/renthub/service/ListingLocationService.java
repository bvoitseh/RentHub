package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.ListingLocationResponse;
import com.bohakka.renthub.dto.PinAddressResponse;

/** FR-4 (D-48, D-49): the pin and postal code for a listing address, and the address under a moved pin. */
public interface ListingLocationService {

	ListingLocationResponse locate(Long settlementId, String street);

	PinAddressResponse describe(Long settlementId, double lat, double lon);
}
