package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.EquipmentStatus;
import java.math.BigDecimal;

/**
 * A catalogue or dashboard card; {@code distanceKm} is set when the query had a point, {@code totalPrice} when it had
 * rental dates (FR-7: days × units × price per day); {@code coverUrl} may be null.
 */
public record EquipmentSummaryResponse(Long id, String title, BigDecimal pricePerDay, String categoryName,
		String address, String coverUrl, int quantity, int minDays, Integer maxDays, boolean pickupAvailable,
		boolean deliveryAvailable, EquipmentStatus status, OwnerBadge owner, Double distanceKm,
		BigDecimal totalPrice) {
}
