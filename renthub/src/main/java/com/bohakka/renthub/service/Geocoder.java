package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.GeocodeResponse;
import com.bohakka.renthub.dto.ReverseGeocodeResponse;
import java.util.Optional;

/** FR-4: a street address to coordinates (structured search) and back; empty when unknown or unavailable. */
public interface Geocoder {

	/** {@code region} may be null (Kyiv, Sevastopol). */
	Optional<GeocodeResponse> geocode(String street, String settlement, String region);

	/** The street and house at a point (D-49). */
	Optional<ReverseGeocodeResponse> reverse(double lat, double lon);
}
