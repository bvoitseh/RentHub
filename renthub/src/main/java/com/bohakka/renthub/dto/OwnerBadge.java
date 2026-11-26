package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;

/**
 * The owner as shown on cards (FR-1a label «Компанія» / «Приватна особа», FR-19 rating from published reviews;
 * {@code rating} is null while there are none).
 */
public record OwnerBadge(Long id, String name, String city, OwnerType ownerType, String companyName, Double rating,
		int reviewCount) {
}
