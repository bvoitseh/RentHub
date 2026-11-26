package com.bohakka.renthub.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * Cached Nominatim answer for one normalised address query (NFR-13: results must be cached). A miss is cached too,
 * for a shorter time, so a failing address is not sent to Nominatim again and again.
 */
@RedisHash("geocode_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeocodeCacheEntry {

	private static final long FOUND_TTL_SECONDS = 30L * 24 * 3600;
	private static final long MISS_TTL_SECONDS = 24L * 3600;

	@Id
	private String query;

	private boolean found;
	private Double lat;
	private Double lon;
	private String displayName;
	private String city;

	@TimeToLive
	private Long ttlSeconds;

	public static GeocodeCacheEntry found(String query, double lat, double lon, String displayName, String city) {
		GeocodeCacheEntry entry = new GeocodeCacheEntry();
		entry.query = query;
		entry.found = true;
		entry.lat = lat;
		entry.lon = lon;
		entry.displayName = displayName;
		entry.city = city;
		entry.ttlSeconds = FOUND_TTL_SECONDS;
		return entry;
	}

	public static GeocodeCacheEntry miss(String query) {
		GeocodeCacheEntry entry = new GeocodeCacheEntry();
		entry.query = query;
		entry.ttlSeconds = MISS_TTL_SECONDS;
		return entry;
	}
}
