package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.BookingRequest;
import com.bohakka.renthub.dto.BookingResponse;

/** Booking requests and the decisions on them (FR-10…FR-14); every call checks who the caller is in the booking. */
public interface BookingService {

	BookingResponse create(Long renterId, BookingRequest request);

	BookingResponse confirm(Long ownerId, Long bookingId);

	BookingResponse reject(Long ownerId, Long bookingId);

	/** The renter (FR-13) or the owner with a reason (FR-13a) cancels before the rental starts. */
	BookingResponse cancel(Long userId, Long bookingId, String reason);
}
