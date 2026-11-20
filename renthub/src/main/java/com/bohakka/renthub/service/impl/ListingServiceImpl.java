package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentRequest;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import com.bohakka.renthub.entity.Category;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentPhoto;
import com.bohakka.renthub.entity.GeoPoint;
import com.bohakka.renthub.entity.RentalTerms;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.StoredObjectsRemoved;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.CategoryRepository;
import com.bohakka.renthub.repository.EquipmentPhotoRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.ListingService;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link ListingService}: the owner's create/edit/archive/delete use cases (FR-4, FR-5). */
@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

	private final EquipmentRepository equipment;
	private final EquipmentPhotoRepository photos;
	private final CategoryRepository categories;
	private final UserRepository users;
	private final EquipmentAccess access;
	private final EquipmentMapper mapper;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
	@Transactional
	public EquipmentDetailsResponse create(Long ownerId, EquipmentRequest request) {
		User owner = users.findById(ownerId)
			.orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
		access.assertMayPublish(owner);
		Equipment listing = new Equipment(owner, category(request.categoryId()), request.title(), request.description(),
			request.condition(), terms(request), request.address(), new GeoPoint(request.lat(), request.lon()),
			clock.instant());
		equipment.save(listing);
		return mapper.details(listing, ownerId);
	}

	@Override
	@Transactional
	public EquipmentDetailsResponse update(Long ownerId, Long equipmentId, EquipmentRequest request) {
		Equipment listing = access.owned(ownerId, equipmentId);
		RentalTerms terms = terms(request);
		if (terms.differsInFrozenTerms(listing.terms())) {
			assertNoActiveBookings(listing);
		}
		listing.describe(category(request.categoryId()), request.title(), request.description(), request.condition(),
			clock.instant());
		listing.applyTerms(terms, clock.instant());
		listing.relocate(request.address(), new GeoPoint(request.lat(), request.lon()), clock.instant());
		return mapper.details(listing, ownerId);
	}

	@Override
	@Transactional
	public EquipmentDetailsResponse archive(Long ownerId, Long equipmentId) {
		Equipment listing = access.owned(ownerId, equipmentId);
		listing.archive(clock.instant());
		return mapper.details(listing, ownerId);
	}

	/** FR-5: only a listing without any bookings may be deleted; its photo files go after the commit. */
	@Override
	@Transactional
	public void delete(Long ownerId, Long equipmentId) {
		Equipment listing = access.owned(ownerId, equipmentId);
		assertHasNoBookings(listing);
		List<String> keys = listing.photoKeys();
		equipment.delete(listing);
		events.publishEvent(new StoredObjectsRemoved(keys));
	}

	@Override
	@Transactional(readOnly = true)
	public List<EquipmentSummaryResponse> listMine(Long ownerId) {
		List<Equipment> mine = equipment.findByOwnerIdOrderByCreatedAtDesc(ownerId);
		Map<Long, String> covers = photos.findCovers(mine.stream().map(Equipment::getId).toList()).stream()
			.collect(Collectors.toMap(p -> p.getEquipment().getId(), EquipmentPhoto::getStorageKey));
		return mine.stream().map(e -> mapper.summary(e, covers.get(e.getId()), null)).toList();
	}

	private Category category(Long categoryId) {
		return categories.findById(categoryId)
			.orElseThrow(() -> new BusinessRuleException("equipment.category_invalid", "equipment.category_invalid"));
	}

	private static RentalTerms terms(EquipmentRequest request) {
		return new RentalTerms(request.pricePerDay(), request.quantity(), request.minDays(), request.maxDays(),
			request.pickupAvailable(), request.deliveryAvailable(), request.deliveryNote());
	}

	/**
	 * FR-5: price and rental terms are frozen while the listing has NEW or CONFIRMED bookings with dateTo >= today
	 * (409 listing them). Bookings do not exist yet; the availability-booking feature fills this in (D-32).
	 */
	private void assertNoActiveBookings(Equipment listing) {
		// Intentionally empty until the availability-booking feature.
	}

	/** FR-5: deletion only without any bookings; filled in by the availability-booking feature (D-32). */
	private void assertHasNoBookings(Equipment listing) {
		// Intentionally empty until the availability-booking feature.
	}
}
