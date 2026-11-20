package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentPhoto;
import com.bohakka.renthub.entity.EquipmentStatus;
import com.bohakka.renthub.entity.GeoPoint;
import com.bohakka.renthub.entity.OwnerType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Composable catalogue filters (FR-6, FR-7) as JPA Specifications. The distance is a spherical-law-of-cosines
 * expression in portable SQL functions (RADIANS, SIN, COS, ACOS, LEAST), so it runs on H2 and PostgreSQL alike.
 */
public final class EquipmentSpecifications {

	private EquipmentSpecifications() {
	}

	/** FR-6, FR-3c: active listings with at least one photo whose owner is neither blocked nor deactivated. */
	public static Specification<Equipment> visibleInCatalogue() {
		return (root, query, cb) -> {
			Subquery<Long> photo = query.subquery(Long.class);
			Root<EquipmentPhoto> photos = photo.from(EquipmentPhoto.class);
			photo.select(photos.get("id")).where(cb.equal(photos.get("equipment"), root));
			return cb.and(cb.equal(root.get("status"), EquipmentStatus.ACTIVE),
				cb.isFalse(root.get("owner").get("blocked")), cb.isNull(root.get("owner").get("deactivatedAt")),
				cb.exists(photo));
		};
	}

	/** Keywords in the title or description, case-insensitive; LIKE wildcards in the input are matched literally. */
	public static Specification<Equipment> matchesText(String text) {
		String pattern = "%" + text.trim().toLowerCase(Locale.ROOT)
			.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
		return (root, query, cb) -> cb.or(cb.like(cb.lower(root.get("title")), pattern, '\\'),
			cb.like(cb.lower(root.get("description")), pattern, '\\'));
	}

	public static Specification<Equipment> inCategory(Long categoryId) {
		return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
	}

	public static Specification<Equipment> priceAtLeast(BigDecimal min) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("pricePerDay"), min);
	}

	public static Specification<Equipment> priceAtMost(BigDecimal max) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("pricePerDay"), max);
	}

	/** FR-7: the rental period fits the listing's minDays/maxDays. */
	public static Specification<Equipment> allowsDuration(int days) {
		return (root, query, cb) -> cb.and(cb.lessThanOrEqualTo(root.get("minDays"), days),
			cb.or(cb.isNull(root.get("maxDays")), cb.greaterThanOrEqualTo(root.get("maxDays"), days)));
	}

	public static Specification<Equipment> hasUnits(int units) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("quantity"), units);
	}

	public static Specification<Equipment> pickupAvailable() {
		return (root, query, cb) -> cb.isTrue(root.get("pickupAvailable"));
	}

	public static Specification<Equipment> deliveryAvailable() {
		return (root, query, cb) -> cb.isTrue(root.get("deliveryAvailable"));
	}

	public static Specification<Equipment> ownedByType(OwnerType type) {
		return (root, query, cb) -> cb.equal(root.get("owner").get("ownerType"), type);
	}

	public static Specification<Equipment> withinKm(GeoPoint point, double radiusKm) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(distanceKm(root, cb, point), radiusKm);
	}

	/**
	 * Nearest first. Ordering is added only to the data query; Spring Data's count query reuses the predicate
	 * without it, and an explicit {@code Sort} in the {@code Pageable} would override it (none is passed).
	 */
	public static Specification<Equipment> nearestFirst(GeoPoint point) {
		return (root, query, cb) -> {
			if (!Long.class.equals(query.getResultType())) {
				query.orderBy(cb.asc(distanceKm(root, cb, point)), cb.desc(root.get("id")));
			}
			return cb.conjunction();
		};
	}

	private static Expression<Double> distanceKm(Root<Equipment> root, CriteriaBuilder cb, GeoPoint point) {
		double pointLat = Math.toRadians(point.lat());
		double pointLon = Math.toRadians(point.lon());
		Expression<Double> lat = cb.function("radians", Double.class, root.get("lat"));
		Expression<Double> lon = cb.function("radians", Double.class, root.get("lon"));
		Expression<Double> cosTerm = cb.prod(cb.prod(cb.function("cos", Double.class, lat),
			cb.function("cos", Double.class, cb.diff(lon, pointLon))), Math.cos(pointLat));
		Expression<Double> sinTerm = cb.prod(cb.function("sin", Double.class, lat), Math.sin(pointLat));
		Expression<Double> cosine = cb.function("least", Double.class, cb.sum(cosTerm, sinTerm), cb.literal(1.0));
		return cb.prod(cb.function("acos", Double.class, cosine), GeoPoint.EARTH_RADIUS_KM);
	}
}
