package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.exception.BusinessRuleException;
import org.springframework.data.domain.Sort;

/**
 * FR-7 sort options; without one, distance if a point is known, else price ascending; ties go to the newest listing.
 * {@code newest} is an extra option; {@code rating} uses the owner's average from published reviews (FR-19);
 * {@code distance} and {@code rating} are ordered inside the query (see EquipmentSpecifications), so they have no
 * {@link Sort} here.
 */
enum CatalogSort {
	NEWEST(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
	PRICE_ASC(Sort.by(Sort.Order.asc("pricePerDay"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
	PRICE_DESC(Sort.by(Sort.Order.desc("pricePerDay"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
	DISTANCE(Sort.unsorted()),
	/** FR-7: owner rating descending, unrated owners last; ordered by the query itself (bestRatedFirst). */
	RATING(Sort.unsorted());

	private final Sort sort;

	CatalogSort(Sort sort) {
		this.sort = sort;
	}

	Sort sort() {
		return sort;
	}

	/** FR-7 default when no sort is given: distance if the user's location is known, otherwise price ascending. */
	static CatalogSort parse(String value, boolean locationKnown) {
		if (value == null || value.isBlank()) {
			return locationKnown ? DISTANCE : PRICE_ASC;
		}
		return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
			case "price,asc", "price" -> PRICE_ASC;
			case "price,desc" -> PRICE_DESC;
			case "distance" -> DISTANCE;
			case "rating" -> RATING;
			case "newest" -> NEWEST;
			default -> throw new BusinessRuleException("catalog.sort_invalid", "catalog.sort_invalid");
		};
	}
}
