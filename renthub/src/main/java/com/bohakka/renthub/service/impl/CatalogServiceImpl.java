package com.bohakka.renthub.service.impl;

import static com.bohakka.renthub.repository.EquipmentSpecifications.allowsDuration;
import static com.bohakka.renthub.repository.EquipmentSpecifications.deliveryAvailable;
import static com.bohakka.renthub.repository.EquipmentSpecifications.hasFreeUnits;
import static com.bohakka.renthub.repository.EquipmentSpecifications.hasUnits;
import static com.bohakka.renthub.repository.EquipmentSpecifications.inCategory;
import static com.bohakka.renthub.repository.EquipmentSpecifications.matchesText;
import static com.bohakka.renthub.repository.EquipmentSpecifications.nearestFirst;
import static com.bohakka.renthub.repository.EquipmentSpecifications.ownedByType;
import static com.bohakka.renthub.repository.EquipmentSpecifications.pickupAvailable;
import static com.bohakka.renthub.repository.EquipmentSpecifications.priceAtLeast;
import static com.bohakka.renthub.repository.EquipmentSpecifications.priceAtMost;
import static com.bohakka.renthub.repository.EquipmentSpecifications.totalAtLeast;
import static com.bohakka.renthub.repository.EquipmentSpecifications.totalAtMost;
import static com.bohakka.renthub.repository.EquipmentSpecifications.visibleInCatalogue;
import static com.bohakka.renthub.repository.EquipmentSpecifications.withinKm;

import com.bohakka.renthub.dto.CatalogQuery;
import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentPhoto;
import com.bohakka.renthub.entity.GeoPoint;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.properties.BookingProperties;
import com.bohakka.renthub.repository.EquipmentPhotoRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.service.CatalogService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** {@link CatalogService}: builds the FR-7 filter from composable Specifications and pages the result. */
@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

	/** FR-7: "pagination with a fixed page size (default 20)". */
	private static final int DEFAULT_PAGE_SIZE = 20;

	private final EquipmentRepository equipment;
	private final EquipmentPhotoRepository photos;
	private final EquipmentMapper mapper;
	private final BookingProperties bookingProperties;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public PageResponse<EquipmentSummaryResponse> search(CatalogQuery query) {
		GeoPoint point = point(query);
		CatalogSort sort = CatalogSort.parse(query.sort(), point != null);
		if (point == null && (query.radiusKm() != null || sort == CatalogSort.DISTANCE)) {
			throw rule("catalog.point_required");
		}
		Specification<Equipment> spec = filter(query, point);
		if (sort == CatalogSort.DISTANCE) {
			spec = spec.and(nearestFirst(point));
		}
		int page = query.page() == null ? 0 : query.page();
		int size = query.size() == null ? DEFAULT_PAGE_SIZE : query.size();
		Page<Equipment> result = equipment.findAll(spec, PageRequest.of(page, size, sort.sort()));
		Map<Long, String> covers = photos.findCovers(result.map(Equipment::getId).getContent()).stream()
			.collect(Collectors.toMap(p -> p.getEquipment().getId(), EquipmentPhoto::getStorageKey));
		Long multiplier = totalMultiplier(query);
		return PageResponse.of(result, e -> mapper.summary(e, covers.get(e.getId()), distance(point, e),
			multiplier == null ? null : e.getPricePerDay().multiply(BigDecimal.valueOf(multiplier))));
	}

	@Override
	@Transactional(readOnly = true)
	public EquipmentDetailsResponse card(Long equipmentId, Long viewerId) {
		Equipment listing = equipment.findWithDetailsById(equipmentId)
			.filter(e -> e.isPublic() || viewerId != null && e.isOwnedBy(viewerId))
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
		return mapper.details(listing, viewerId);
	}

	private Specification<Equipment> filter(CatalogQuery query, GeoPoint point) {
		Specification<Equipment> spec = visibleInCatalogue();
		if (StringUtils.hasText(query.q())) {
			spec = spec.and(matchesText(query.q()));
		}
		if (query.categoryId() != null) {
			spec = spec.and(inCategory(query.categoryId()));
		}
		if (query.priceMin() != null && query.priceMax() != null && query.priceMin().compareTo(query.priceMax()) > 0) {
			throw rule("catalog.price_range_invalid");
		}
		Integer days = rentalDays(query.dateFrom(), query.dateTo());
		Long multiplier = totalMultiplier(query);
		if (query.priceMin() != null) {
			spec = spec.and(multiplier == null ? priceAtLeast(query.priceMin())
				: totalAtLeast(query.priceMin(), multiplier));
		}
		if (query.priceMax() != null) {
			spec = spec.and(multiplier == null ? priceAtMost(query.priceMax())
				: totalAtMost(query.priceMax(), multiplier));
		}
		int units = query.units() == null ? 1 : query.units();
		if (query.units() != null) {
			spec = spec.and(hasUnits(units));
		}
		if (days != null) {
			spec = spec.and(allowsDuration(days)).and(hasFreeUnits(units, query.dateFrom(), query.dateTo(),
				clock.instant().minus(bookingProperties.requestTtl()), LocalDate.now(clock)));
		}
		if (point != null && query.radiusKm() != null) {
			spec = spec.and(withinKm(point, query.radiusKm()));
		}
		if (Boolean.TRUE.equals(query.pickup())) {
			spec = spec.and(pickupAvailable());
		}
		if (Boolean.TRUE.equals(query.delivery())) {
			spec = spec.and(deliveryAvailable());
		}
		if (query.ownerType() != null) {
			spec = spec.and(ownedByType(query.ownerType()));
		}
		return spec;
	}

	/**
	 * FR-7, NFR-10: both dates or none, from today on, at least one day.
	 */
	private Integer rentalDays(LocalDate from, LocalDate to) {
		if (from == null && to == null) {
			return null;
		}
		if (from == null || to == null || to.isBefore(from) || from.isBefore(LocalDate.now(clock))) {
			throw rule("catalog.dates_invalid");
		}
		return Math.toIntExact(ChronoUnit.DAYS.between(from, to) + 1);
	}

	/**
	 * FR-7: with rental dates the price range and the card price refer to the total cost, days × units × price per
	 * day (units default to 1); sorting by price per day gives the same order. Null without dates.
	 */
	private Long totalMultiplier(CatalogQuery query) {
		Integer days = rentalDays(query.dateFrom(), query.dateTo());
		return days == null ? null : (long) days * (query.units() == null ? 1 : query.units());
	}

	private static GeoPoint point(CatalogQuery query) {
		if (query.lat() == null && query.lon() == null) {
			return null;
		}
		if (query.lat() == null || query.lon() == null) {
			throw rule("catalog.point_required");
		}
		return new GeoPoint(query.lat(), query.lon());
	}

	private static Double distance(GeoPoint point, Equipment e) {
		return point == null ? null : Math.round(point.distanceKmTo(e.location()) * 10) / 10.0;
	}

	private static BusinessRuleException rule(String key) {
		return new BusinessRuleException(key, key);
	}
}
