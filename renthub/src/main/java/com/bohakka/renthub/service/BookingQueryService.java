package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.BookingResponse;
import com.bohakka.renthub.dto.ContactResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.BookingStatus;
import java.util.Set;

/** What a user sees of bookings: their own requests, requests for their listings, the other party's contact. */
public interface BookingQueryService {

	PageResponse<BookingResponse> mine(Long renterId, int page);

	/** {@code statuses} empty means all. */
	PageResponse<BookingResponse> incoming(Long ownerId, Set<BookingStatus> statuses, int page);

	/** FR-3b: only for the renter or owner of a CONFIRMED or COMPLETED booking. */
	ContactResponse contact(Long userId, Long bookingId);
}
