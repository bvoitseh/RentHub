package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.AvailabilityBlockRequest;
import com.bohakka.renthub.dto.AvailabilityBlockResponse;
import java.util.List;

/** The owner's availability calendar (FR-8): close and reopen date ranges of an own listing. */
public interface AvailabilityBlockService {

	List<AvailabilityBlockResponse> list(Long ownerId, Long equipmentId);

	AvailabilityBlockResponse create(Long ownerId, Long equipmentId, AvailabilityBlockRequest request);

	void delete(Long ownerId, Long equipmentId, Long blockId);
}
