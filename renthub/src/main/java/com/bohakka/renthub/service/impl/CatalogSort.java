package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.exception.BusinessRuleException;
import org.springframework.data.domain.Sort;

/**
 * FR-7 sort options. {@code rating} orders by newest until the reviews feature provides ratings (DECISIONS.md D-32);
 * {@code distance} is ordered by the distance expression itself, so it has no {@link Sort} here.
 */
enum CatalogSort {
	NEWEST(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
	PRICE_ASC(Sort.by(Sort.Order.asc("pricePerDay"), Sort.Order.desc("id"))),
	PRICE_DESC(Sort.by(Sort.Order.desc("pricePerDay"), Sort.Order.desc("id"))),
	DISTANCE(Sort.unsorted()),
	RATING(NEWEST.sort);

	private final Sort sort;

	CatalogSort(Sort sort) {
		this.sort = sort;
	}

	Sort sort() {
		return sort;
	}

	static CatalogSort parse(String value) {
		if (value == null || value.isBlank()) {
			return NEWEST;
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
