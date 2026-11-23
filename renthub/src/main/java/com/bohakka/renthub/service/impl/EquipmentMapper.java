package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.CategoryResponse;
import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import com.bohakka.renthub.dto.OwnerBadge;
import com.bohakka.renthub.dto.PhotoResponse;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.service.ObjectStorage;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps listings to API records inside the service transaction; photo keys become public storage URLs. */
@Component
@RequiredArgsConstructor
public class EquipmentMapper {

	private final ObjectStorage storage;

	/** FR-19 rating and review count arrive with the reviews feature; until then: no rating, 0 reviews. */
	public OwnerBadge owner(User owner) {
		return new OwnerBadge(owner.getId(), owner.getName(), owner.getCity(), owner.getOwnerType(),
			owner.getCompanyName(), null, 0);
	}

	public EquipmentSummaryResponse summary(Equipment e, String coverKey, Double distanceKm, BigDecimal totalPrice) {
		return new EquipmentSummaryResponse(e.getId(), e.getTitle(), e.getPricePerDay(), e.getCategory().getName(),
			e.getAddress(), coverKey == null ? null : storage.publicUrl(coverKey), e.getQuantity(), e.getMinDays(),
			e.getMaxDays(), e.isPickupAvailable(), e.isDeliveryAvailable(), e.getStatus(), owner(e.getOwner()),
			distanceKm, totalPrice);
	}

	public EquipmentDetailsResponse details(Equipment e, Long viewerId) {
		return new EquipmentDetailsResponse(e.getId(), e.getTitle(), e.getDescription(),
			CategoryResponse.from(e.getCategory()), e.getCondition(), e.getPricePerDay(), e.getQuantity(),
			e.getMinDays(), e.getMaxDays(), e.isPickupAvailable(), e.isDeliveryAvailable(), e.getDeliveryNote(),
			e.getAddress(), e.getLat(), e.getLon(), e.getStatus(), photos(e), owner(e.getOwner()),
			viewerId != null && e.isOwnedBy(viewerId), e.getCreatedAt());
	}

	public List<PhotoResponse> photos(Equipment e) {
		return e.getPhotos().stream()
			.map(p -> new PhotoResponse(p.getId(), storage.publicUrl(p.getStorageKey()), p.getPosition()))
			.toList();
	}
}
