package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.EquipmentStatus;
import com.bohakka.renthub.entity.ItemCondition;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** The equipment card (FR-6); {@code mine} tells the SPA to show the owner's actions. */
public record EquipmentDetailsResponse(Long id, String title, String description, CategoryResponse category,
		ItemCondition condition, BigDecimal pricePerDay, int quantity, int minDays, Integer maxDays,
		boolean pickupAvailable, boolean deliveryAvailable, String deliveryNote, String address, String city,
		SettlementResponse settlement, String postalCode, double lat,
		double lon,
		EquipmentStatus status, List<PhotoResponse> photos, VideoResponse video, OwnerBadge owner, boolean mine, Instant createdAt) {
}
