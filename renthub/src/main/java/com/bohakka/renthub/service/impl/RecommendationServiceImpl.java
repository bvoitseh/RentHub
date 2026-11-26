package com.bohakka.renthub.service.impl;

import static com.bohakka.renthub.repository.EquipmentSpecifications.nearestThenSeason;
import static com.bohakka.renthub.repository.EquipmentSpecifications.notOwnedBy;
import static com.bohakka.renthub.repository.EquipmentSpecifications.visibleInCatalogue;
import static com.bohakka.renthub.repository.EquipmentSpecifications.withinKm;

import com.bohakka.renthub.dto.RecommendationsResponse;
import com.bohakka.renthub.dto.RecommendationsResponse.Origin;
import com.bohakka.renthub.dto.RecommendationsResponse.Source;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentPhoto;
import com.bohakka.renthub.entity.GeoPoint;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.properties.RecommendationProperties;
import com.bohakka.renthub.repository.EquipmentPhotoRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.Geocoder;
import com.bohakka.renthub.service.RecommendationService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * {@link RecommendationService}. The point is the browser location (consent only), else the geocoded profile city,
 * else the start city (FR-16). Public listings within the radius, not the viewer's own, ordered nearest first, then
 * in-season categories, then owner rating, then newest (author's order, D-44). Not transactional on purpose: the
 * city lookup may call Nominatim, which must not hold a database transaction; the queries fetch what they map.
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

	private final EquipmentRepository equipment;
	private final EquipmentPhotoRepository photos;
	private final UserRepository users;
	private final Geocoder geocoder;
	private final EquipmentMapper mapper;
	private final RecommendationProperties properties;
	private final Clock clock;

	@Override
	public RecommendationsResponse recommend(Long viewerId, Double lat, Double lon) {
		User viewer = viewerId == null ? null : users.findById(viewerId).orElse(null);
		Located located = locate(viewer, lat, lon);
		int month = LocalDate.now(clock).getMonthValue();
		Specification<Equipment> spec = visibleInCatalogue().and(withinKm(located.point(), properties.radiusKm()))
			.and(nearestThenSeason(located.point(), month));
		if (viewer != null) {
			spec = spec.and(notOwnedBy(viewer.getId()));
		}
		List<Equipment> found = equipment.findAll(spec, PageRequest.of(0, properties.limit())).getContent();
		Map<Long, String> covers = found.isEmpty() ? Map.of() : photos.findCovers(found.stream().map(Equipment::getId)
				.toList()).stream()
			.collect(Collectors.toMap(p -> p.getEquipment().getId(), EquipmentPhoto::getStorageKey));
		List<RecommendationsResponse.Item> items = found.stream()
			.map(e -> new RecommendationsResponse.Item(mapper.summary(e, covers.get(e.getId()),
				Math.round(located.point().distanceKmTo(e.location()) * 10) / 10.0, null),
				e.getCategory().isInSeason(month)))
			.toList();
		return new RecommendationsResponse(located.origin(), items);
	}

	private Located locate(User viewer, Double lat, Double lon) {
		if (viewer != null && viewer.isGeoConsent() && lat != null && lon != null) {
			return new Located(new GeoPoint(lat, lon), new Origin(Source.BROWSER, null));
		}
		if (viewer != null && StringUtils.hasText(viewer.getCity())) {
			String city = viewer.getCity().trim();
			var geocoded = geocoder.geocode(city);
			if (geocoded.isPresent()) {
				return new Located(new GeoPoint(geocoded.get().lat(), geocoded.get().lon()), new Origin(Source.CITY, city));
			}
		}
		return new Located(new GeoPoint(properties.startLat(), properties.startLon()),
			new Origin(Source.START_CITY, properties.startCity()));
	}

	private record Located(GeoPoint point, Origin origin) {
	}
}
