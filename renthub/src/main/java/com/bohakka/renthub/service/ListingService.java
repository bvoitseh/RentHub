package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentRequest;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import java.util.List;

/** A user's own listings (FR-4, FR-5): create, edit, archive, delete; every call checks ownership. */
public interface ListingService {

	/** FR-4: only an active user with a verified e-mail may publish (FR-1). */
	EquipmentDetailsResponse create(Long ownerId, EquipmentRequest request);

	EquipmentDetailsResponse update(Long ownerId, Long equipmentId, EquipmentRequest request);

	EquipmentDetailsResponse archive(Long ownerId, Long equipmentId);

	void delete(Long ownerId, Long equipmentId);

	List<EquipmentSummaryResponse> listMine(Long ownerId);
}
