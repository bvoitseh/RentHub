package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.CategoryResponse;
import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import com.bohakka.renthub.dto.OwnerBadge;
import com.bohakka.renthub.dto.PhotoResponse;
import com.bohakka.renthub.dto.VideoResponse;
import com.bohakka.renthub.dto.SettlementResponse;
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

	/** FR-19: rating and review count from published reviews (null rating while there are none). */
	public OwnerBadge owner(User owner) {
		return new OwnerBadge(owner.getId(), owner.getName(), owner.getCity(), owner.getOwnerType(),
			owner.getCompanyName(), rating(owner), owner.getReviewCount());
	}

	public static Double rating(User user) {
		return user.getRatingAvg() == null ? null : user.getRatingAvg().doubleValue();
	}

	public EquipmentSummaryResponse summary(Equipment e, String coverKey, Double distanceKm, BigDecimal totalPrice) {
		return new EquipmentSummaryResponse(e.getId(), e.getTitle(), e.getPricePerDay(), e.getCategory().getName(),
			e.getCity(), e.getAddress(), coverKey == null ? null : storage.publicUrl(coverKey), e.getQuantity(), e.getMinDays(),
			e.getMaxDays(), e.isPickupAvailable(), e.isDeliveryAvailable(), e.getStatus(), owner(e.getOwner()),
			distanceKm, totalPrice);
	}

	public EquipmentDetailsResponse details(Equipment e, Long viewerId) {
		return new EquipmentDetailsResponse(e.getId(), e.getTitle(), e.getDescription(),
			CategoryResponse.from(e.getCategory()), e.getCondition(), e.getPricePerDay(), e.getQuantity(),
			e.getMinDays(), e.getMaxDays(), e.isPickupAvailable(), e.isDeliveryAvailable(), e.getDeliveryNote(),
			e.getAddress(), e.getCity(), SettlementResponse.from(e.getSettlement()), e.getPostalCode(), e.getLat(), e.getLon(), e.getStatus(), photos(e), video(e), owner(e.getOwner()),
			viewerId != null && e.isOwnedBy(viewerId), e.getCreatedAt());
	}

	public VideoResponse video(Equipment e) {
		return e.getVideoKey() == null ? null : new VideoResponse(storage.publicUrl(e.getVideoKey()), e.getVideoContentType());
	}

	public List<PhotoResponse> photos(Equipment e) {
		return e.getPhotos().stream()
			.map(p -> new PhotoResponse(p.getId(), storage.publicUrl(p.getStorageKey()), p.getPosition()))
			.toList();
	}
}
