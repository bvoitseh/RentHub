package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import java.time.Instant;

/**
 * FR-19 public profile: rating and review count from published reviews, owner cancellations (FR-13a) and complaints
 * (FR-21). Those features come later, so for now {@code rating} is null and the counts are 0.
 */
public record PublicProfileResponse(Long id, String name, String city, OwnerType ownerType, String companyName,
		boolean deactivated, Double rating, int reviewCount, int ownerCancellations, int complaintCount,
		Instant memberSince) {
}
