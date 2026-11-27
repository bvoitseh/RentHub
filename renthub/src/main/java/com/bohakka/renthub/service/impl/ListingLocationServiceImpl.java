package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.GeocodeResponse;
import com.bohakka.renthub.dto.ListingLocationResponse;
import com.bohakka.renthub.entity.Settlement;
import com.bohakka.renthub.service.AddressService;
import com.bohakka.renthub.service.Geocoder;
import com.bohakka.renthub.service.ListingLocationService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * {@link ListingLocationService} (D-48). The pin comes from Nominatim's structured street search within the settlement
 * and region, else it is the settlement's own point (not exact). The postal code: villages and селища take the
 * settlement's OSM code; cities (several codes) and settlements without a status take the street result's code and
 * fall back to the OSM one. Not transactional: Nominatim must not hold a database transaction.
 */
@Service
@RequiredArgsConstructor
public class ListingLocationServiceImpl implements ListingLocationService {

	private final AddressService addresses;
	private final Geocoder geocoder;

	@Override
	public ListingLocationResponse locate(Long settlementId, String street) {
		Settlement settlement = addresses.settlement(settlementId);
		Optional<GeocodeResponse> found = StringUtils.hasText(street)
			? geocoder.geocode(street.trim(), settlement.getName(),
				settlement.getRegion().isCity() ? null : settlement.getRegion().getName())
			: Optional.empty();
		String fromStreet = found.map(GeocodeResponse::postalCode).orElse(null);
		String postalCode = settlement.hasSinglePostalCode()
			? firstNonNull(settlement.getPostalCode(), fromStreet)
			: firstNonNull(fromStreet, settlement.getPostalCode());
		return found
			.map(g -> new ListingLocationResponse(g.lat(), g.lon(), g.displayName(), postalCode, true))
			.orElseGet(() -> new ListingLocationResponse(settlement.getLat(), settlement.getLon(), settlement.getLabel(),
				postalCode, false));
	}

	private static String firstNonNull(String first, String second) {
		return first != null ? first : second;
	}
}
