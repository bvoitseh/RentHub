package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.GeocodeResponse;
import com.bohakka.renthub.dto.ListingLocationResponse;
import com.bohakka.renthub.dto.PinAddressResponse;
import com.bohakka.renthub.dto.ReverseGeocodeResponse;
import com.bohakka.renthub.entity.GeoPoint;
import com.bohakka.renthub.entity.Settlement;
import com.bohakka.renthub.properties.GeocodingProperties;
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
 * fall back to the OSM one. A street result farther than {@code app.geocoding.max-distance-km} from the settlement's
 * centre is a wrong match and counts as not found (D-56). A moved pin is described by Nominatim reverse geocoding
 * with the same postal-code rule (D-49). Not transactional: Nominatim must not hold a database transaction.
 */
@Service
@RequiredArgsConstructor
public class ListingLocationServiceImpl implements ListingLocationService {

	private final AddressService addresses;
	private final Geocoder geocoder;
	private final GeocodingProperties properties;

	@Override
	public ListingLocationResponse locate(Long settlementId, String street) {
		Settlement settlement = addresses.settlement(settlementId);
		Optional<GeocodeResponse> found = StringUtils.hasText(street)
			? geocoder.geocode(street.trim(), settlement.getName(),
				settlement.getRegion().isCity() ? null : settlement.getRegion().getName())
				.filter(g -> withinSettlement(settlement, g))
			: Optional.empty();
		String postalCode = postalCode(settlement, found.map(GeocodeResponse::postalCode).orElse(null));
		return found
			.map(g -> new ListingLocationResponse(g.lat(), g.lon(), g.displayName(), postalCode, true))
			.orElseGet(() -> new ListingLocationResponse(settlement.getLat(), settlement.getLon(), settlement.getLabel(),
				postalCode, false));
	}

	@Override
	public PinAddressResponse describe(Long settlementId, double lat, double lon) {
		Settlement settlement = addresses.settlement(settlementId);
		Optional<ReverseGeocodeResponse> found = geocoder.reverse(lat, lon);
		return new PinAddressResponse(found.map(ReverseGeocodeResponse::street).orElse(null),
			postalCode(settlement, found.map(ReverseGeocodeResponse::postalCode).orElse(null)));
	}

	private boolean withinSettlement(Settlement settlement, GeocodeResponse result) {
		return settlement.location().distanceKmTo(new GeoPoint(result.lat(), result.lon())) <= properties.maxDistanceKm();
	}

	private static String postalCode(Settlement settlement, String fromGeocoder) {
		return settlement.hasSinglePostalCode()
			? firstNonNull(settlement.getPostalCode(), fromGeocoder)
			: firstNonNull(fromGeocoder, settlement.getPostalCode());
	}

	private static String firstNonNull(String first, String second) {
		return first != null ? first : second;
	}
}
