package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;

/**
 * The owner as shown on cards (FR-1a label «Компанія» / «Приватна особа», FR-19 rating). Rating and review count
 * come with the reviews feature; until then {@code rating} is null and {@code reviewCount} is 0.
 */
public record OwnerBadge(Long id, String name, String city, OwnerType ownerType, String companyName, Double rating,
		int reviewCount) {
}
