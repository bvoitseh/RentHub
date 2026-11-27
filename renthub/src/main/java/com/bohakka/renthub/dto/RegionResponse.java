package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.Region;

/** A region for the «Область» select (D-48). */
public record RegionResponse(Long id, String name) {

	public static RegionResponse from(Region region) {
		return new RegionResponse(region.getId(), region.getName());
	}
}
