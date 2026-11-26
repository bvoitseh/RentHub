package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.EquipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** FR-22: a listing in the admin list, in any status. */
public record AdminEquipmentResponse(Long id, String title, String categoryName, BigDecimal pricePerDay,
		EquipmentStatus status, Long ownerId, String ownerName, boolean ownerBlocked, Instant createdAt) {
}
