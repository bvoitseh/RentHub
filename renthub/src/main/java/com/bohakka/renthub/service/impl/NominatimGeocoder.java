package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.GeocodeResponse;
import com.bohakka.renthub.entity.GeocodeCacheEntry;
import com.bohakka.renthub.properties.GeocodingProperties;
import com.bohakka.renthub.repository.GeocodeCacheRepository;
import com.bohakka.renthub.service.Geocoder;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * {@link Geocoder} on OpenStreetMap Nominatim within its usage policy (NFR-13): identifying User-Agent (configured on
 * the client), at most one request per second from this instance, and every answer cached in Redis. A provider
 * failure is logged and reported as "not found" without caching, so the owner places the pin by hand (FR-4).
 */
@Slf4j
@Component
public class NominatimGeocoder implements Geocoder {

	private static final long MIN_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

	private final RestClient nominatim;
	private final GeocodeCacheRepository cache;
	private final GeocodingProperties properties;
	private long lastRequestNanos = System.nanoTime() - MIN_INTERVAL_NANOS;

	public NominatimGeocoder(@Qualifier("nominatimRestClient") RestClient nominatim, GeocodeCacheRepository cache,
			GeocodingProperties properties) {
		this.nominatim = nominatim;
		this.cache = cache;
		this.properties = properties;
	}

	@Override
	public Optional<GeocodeResponse> geocode(String address) {
		String key = address.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
		Optional<GeocodeCacheEntry> cached = cache.findById(key);
		if (cached.isPresent()) {
			GeocodeCacheEntry entry = cached.get();
			return entry.isFound()
				? Optional.of(new GeocodeResponse(entry.getLat(), entry.getLon(), entry.getDisplayName(), entry.getCity()))
				: Optional.empty();
		}
		try {
			Optional<GeocodeResponse> result = search(address.trim());
			cache.save(result.map(r -> GeocodeCacheEntry.found(key, r.lat(), r.lon(), r.displayName(), r.city()))
				.orElseGet(() -> GeocodeCacheEntry.miss(key)));
			return result;
		} catch (RestClientException | NumberFormatException ex) {
			log.warn("Geocoding failed: {}", ex.getMessage());
			return Optional.empty();
		}
	}

	private Optional<GeocodeResponse> search(String address) {
		waitForSlot();
		Place[] places = nominatim.get()
			.uri(uri -> uri.path("/search").queryParam("q", address).queryParam("format", "jsonv2")
				.queryParam("limit", 1).queryParam("addressdetails", 1).queryParam("countrycodes", properties.countryCodes())
				.queryParam("accept-language", "uk").build())
			.retrieve()
			.body(Place[].class);
		if (places == null || places.length == 0) {
			return Optional.empty();
		}
		Place place = places[0];
		return Optional.of(new GeocodeResponse(Double.parseDouble(place.lat()), Double.parseDouble(place.lon()),
			place.displayName(), place.address() == null ? null : place.address().settlement()));
	}

	/** NFR-13: at most one Nominatim request per second; concurrent callers queue here. */
	private synchronized void waitForSlot() {
		long waitNanos = lastRequestNanos + MIN_INTERVAL_NANOS - System.nanoTime();
		if (waitNanos > 0) {
			try {
				TimeUnit.NANOSECONDS.sleep(waitNanos);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new RestClientException("Interrupted while waiting for a geocoding slot");
			}
		}
		lastRequestNanos = System.nanoTime();
	}

	private record Place(String lat, String lon, @JsonProperty("display_name") String displayName, Address address) {
	}

	/** Nominatim names the settlement by its kind; the first one present is the listing's city (D-46). */
	private record Address(String city, String town, String village) {

		String settlement() {
			return city != null ? city : town != null ? town : village;
		}
	}
}
