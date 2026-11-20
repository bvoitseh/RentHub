package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.GeocodeResponse;
import java.util.Optional;

/** FR-4: address to coordinates; empty when the address is unknown or the provider is unavailable. */
public interface Geocoder {

	Optional<GeocodeResponse> geocode(String address);
}
