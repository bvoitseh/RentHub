package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.BookingResponse;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentPhoto;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.repository.EquipmentPhotoRepository;
import com.bohakka.renthub.service.ObjectStorage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps bookings to API records inside the service transaction; covers of a page are loaded in one query. */
@Component
@RequiredArgsConstructor
public class BookingMapper {

	private final EquipmentPhotoRepository photos;
	private final ObjectStorage storage;
	private final BookingDeadline deadline;

	public BookingResponse toResponse(Booking booking) {
		return mapper(List.of(booking)).apply(booking);
	}

	/** A mapping function for a page of bookings, with their listings' cover photos fetched up front. */
	public Function<Booking, BookingResponse> mapper(List<Booking> page) {
		List<Long> ids = page.stream().map(b -> b.getEquipment().getId()).distinct().toList();
		Map<Long, String> covers = ids.isEmpty() ? Map.of() : photos.findCovers(ids).stream()
			.collect(Collectors.toMap(p -> p.getEquipment().getId(), EquipmentPhoto::getStorageKey));
		return b -> map(b, covers.get(b.getEquipment().getId()));
	}

	private BookingResponse map(Booking b, String coverKey) {
		Equipment e = b.getEquipment();
		return new BookingResponse(b.getId(),
			new BookingResponse.Item(e.getId(), e.getTitle(), coverKey == null ? null : storage.publicUrl(coverKey)),
			party(b.getRenter()), party(e.getOwner()), b.getUnits(), b.getDateFrom(), b.getDateTo(), b.period().days(),
			b.getHandover(), b.getPricePerDay(), b.getTotalPrice(), b.getCommission(), b.getStatus(), b.isExpired(),
			b.getCreatedAt(), b.getDecidedAt(), deadline.respondBy(b), b.getCancelledBy(), b.getCancelReason());
	}

	private static BookingResponse.Party party(User user) {
		return new BookingResponse.Party(user.getId(), user.getName(), EquipmentMapper.rating(user),
			user.getReviewCount());
	}
}
