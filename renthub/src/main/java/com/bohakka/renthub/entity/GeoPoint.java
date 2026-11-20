package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;

/** A WGS-84 point with the great-circle distance used for "distance" filtering and sorting (FR-7). */
public record GeoPoint(double lat, double lon) {

	public static final double EARTH_RADIUS_KM = 6371.0;

	public GeoPoint {
		if (Double.isNaN(lat) || Double.isNaN(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
			throw new BusinessRuleException("geo.invalid_point", "geo.invalid_point");
		}
	}

	/** Haversine distance in kilometres. */
	public double distanceKmTo(GeoPoint other) {
		double dLat = Math.toRadians(other.lat - lat);
		double dLon = Math.toRadians(other.lon - lon);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
			+ Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
	}
}
