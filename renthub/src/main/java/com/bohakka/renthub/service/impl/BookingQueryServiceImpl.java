package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.BookingResponse;
import com.bohakka.renthub.dto.ContactResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.service.BookingQueryService;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link BookingQueryService}: lists of the caller's bookings and the FR-3b contact rule. */
@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

	private static final int PAGE_SIZE = 20;
	private static final Set<BookingStatus> CONTACT_VISIBLE = EnumSet.of(BookingStatus.CONFIRMED,
		BookingStatus.COMPLETED);

	private final BookingRepository bookings;
	private final BookingMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public PageResponse<BookingResponse> mine(Long renterId, int page) {
		return toPage(bookings.findByRenterIdOrderByCreatedAtDesc(renterId, PageRequest.of(page, PAGE_SIZE)));
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<BookingResponse> incoming(Long ownerId, Set<BookingStatus> statuses, int page) {
		Set<BookingStatus> filter = statuses == null || statuses.isEmpty() ? EnumSet.allOf(BookingStatus.class)
			: statuses;
		return toPage(bookings.findIncoming(ownerId, filter, PageRequest.of(page, PAGE_SIZE)));
	}

	@Override
	@Transactional(readOnly = true)
	public ContactResponse contact(Long userId, Long bookingId) {
		Booking booking = bookings.findWithPartiesById(bookingId)
			.orElseThrow(() -> new NotFoundException("booking.not_found", "booking.not_found"));
		User other;
		if (booking.isRenter(userId)) {
			other = booking.getEquipment().getOwner();
		} else if (booking.isOwner(userId)) {
			other = booking.getRenter();
		} else {
			throw new ForbiddenException("booking.not_participant", "booking.not_participant");
		}
		if (!CONTACT_VISIBLE.contains(booking.getStatus())) {
			throw new ForbiddenException("booking.contact_unavailable", "booking.contact_unavailable");
		}
		return new ContactResponse(other.getId(), other.getName(), other.getPhone());
	}

	private PageResponse<BookingResponse> toPage(Page<Booking> page) {
		return PageResponse.of(page, mapper.mapper(page.getContent()));
	}
}
