package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import java.time.Instant;

/**
 * FR-19 public profile: rating and review count from published reviews ({@code rating} null while there are none),
 * owner cancellations (FR-13a) and complaints (FR-21; 0 until that feature).
 */
public record PublicProfileResponse(Long id, String name, String city, OwnerType ownerType, String companyName,
		boolean deactivated, Double rating, int reviewCount, int ownerCancellations, int complaintCount,
		Instant memberSince) {
}
