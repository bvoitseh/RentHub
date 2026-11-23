package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.AvailabilityBlockRequest;
import com.bohakka.renthub.dto.AvailabilityBlockResponse;
import com.bohakka.renthub.dto.BookingRef;
import com.bohakka.renthub.entity.AvailabilityBlock;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingPeriod;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.properties.BookingProperties;
import com.bohakka.renthub.repository.AvailabilityBlockRepository;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.service.AvailabilityBlockService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AvailabilityBlockService}. A block may not overlap another block, nor days already held by a NEW or CONFIRMED
 * booking (D-40: the owner rejects or cancels those first). Creating a block takes the listing's row lock, so it
 * cannot race a booking request for the same days (FR-11).
 */
@Service
@RequiredArgsConstructor
public class AvailabilityBlockServiceImpl implements AvailabilityBlockService {

	private final EquipmentRepository equipment;
	private final AvailabilityBlockRepository blocks;
	private final BookingRepository bookings;
	private final EquipmentAccess access;
	private final BookingProperties properties;
	private final BookingDeadline deadline;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public List<AvailabilityBlockResponse> list(Long ownerId, Long equipmentId) {
		access.owned(ownerId, equipmentId);
		return blocks.findByEquipmentIdAndDateToGreaterThanEqualOrderByDateFromAsc(equipmentId, LocalDate.now(clock))
			.stream().map(AvailabilityBlockResponse::from).toList();
	}

	@Override
	@Transactional
	public AvailabilityBlockResponse create(Long ownerId, Long equipmentId, AvailabilityBlockRequest request) {
		Equipment listing = lockOwned(ownerId, equipmentId);
		BookingPeriod period = new BookingPeriod(request.dateFrom(), request.dateTo());
		LocalDate today = LocalDate.now(clock);
		if (period.from().isBefore(today) || period.to().isAfter(today.plusDays(properties.horizonDays()))) {
			throw new BusinessRuleException("block.dates_invalid", "block.dates_invalid", properties.horizonDays());
		}
		if (!blocks.findOverlapping(equipmentId, period.from(), period.to()).isEmpty()) {
			throw new ConflictException("block.overlaps", "block.overlaps");
		}
		List<Booking> held = bookings.findOverlapping(equipmentId, BookingStatus.HOLDING_UNITS, period.from(),
			period.to()).stream().filter(b -> !deadline.hasPassed(b)).toList();
		if (!held.isEmpty()) {
			throw new ConflictException("block.has_bookings", "block.has_bookings")
				.withProperty("bookings", held.stream().map(BookingRef::of).toList());
		}
		AvailabilityBlock block = new AvailabilityBlock(listing, period, request.reason(), clock.instant());
		return AvailabilityBlockResponse.from(blocks.save(block));
	}

	@Override
	@Transactional
	public void delete(Long ownerId, Long equipmentId, Long blockId) {
		access.owned(ownerId, equipmentId);
		AvailabilityBlock block = blocks.findById(blockId)
			.filter(b -> b.getEquipment().getId().equals(equipmentId))
			.orElseThrow(() -> new NotFoundException("block.not_found", "block.not_found"));
		blocks.delete(block);
	}

	private Equipment lockOwned(Long ownerId, Long equipmentId) {
		Equipment listing = equipment.lockById(equipmentId)
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
		if (!listing.isOwnedBy(ownerId)) {
			throw new ForbiddenException("equipment.not_owner", "equipment.not_owner");
		}
		return listing;
	}
}
