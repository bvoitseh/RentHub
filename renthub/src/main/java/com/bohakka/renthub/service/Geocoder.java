package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.GeocodeResponse;
import java.util.Optional;

/** FR-4: a street address to coordinates (structured search); empty when unknown or the provider is unavailable. */
public interface Geocoder {

	/** {@code region} may be null (Kyiv, Sevastopol). */
	Optional<GeocodeResponse> geocode(String street, String settlement, String region);
}
