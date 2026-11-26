package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.ComplaintRequest;
import com.bohakka.renthub.dto.ComplaintResponse;

/** FR-21: users complain about a booking or a listing. */
public interface ComplaintService {

	ComplaintResponse create(Long authorId, ComplaintRequest request);
}
