package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.AvailabilityBlock;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentPhoto;
import com.bohakka.renthub.entity.EquipmentStatus;
import com.bohakka.renthub.entity.GeoPoint;
import com.bohakka.renthub.entity.OwnerType;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Composable catalogue filters (FR-6, FR-7) as JPA Specifications. The distance is a spherical-law-of-cosines
 * expression in portable SQL functions (RADIANS, SIN, COS, ACOS, LEAST), so it stays plain portable SQL.
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

	/** FR-7 total-cost mode: {@code multiplier} = days × units, compared against the total price range. */
	public static Specification<Equipment> totalAtLeast(BigDecimal min, long multiplier) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(
			cb.prod(root.<BigDecimal>get("pricePerDay"), BigDecimal.valueOf(multiplier)), min);
	}

	public static Specification<Equipment> totalAtMost(BigDecimal max, long multiplier) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(
			cb.prod(root.<BigDecimal>get("pricePerDay"), BigDecimal.valueOf(multiplier)), max);
	}

	/**
	 * FR-7, FR-9: {@code units} are free on every day of {@code [from, to]}: no block overlaps the period, and on no day
	 * do the holding bookings (CONFIRMED, or NEW still within its FR-15b deadline: made after {@code requestsAfter}
	 * and starting after {@code today}, D-39) plus {@code units} exceed the quantity. The busiest day of a period is its
	 * first day or the first day of a booking starting inside it, so only those days are checked.
	 */
	public static Specification<Equipment> hasFreeUnits(int units, LocalDate from, LocalDate to, Instant requestsAfter,
			LocalDate today) {
		Holding holding = new Holding(requestsAfter, today);
		return (root, query, cb) -> {
			Subquery<Long> block = query.subquery(Long.class);
			Root<AvailabilityBlock> b = block.from(AvailabilityBlock.class);
			block.select(b.get("id")).where(cb.equal(b.get("equipment"), root),
				cb.lessThanOrEqualTo(b.get("dateFrom"), to), cb.greaterThanOrEqualTo(b.get("dateTo"), from));

			Subquery<Long> busyStart = query.subquery(Long.class);
			Root<Booking> start = busyStart.from(Booking.class);
			busyStart.select(start.get("id")).where(cb.equal(start.get("equipment"), root),
				holding.of(cb, start), cb.greaterThan(start.get("dateFrom"), from),
				cb.lessThanOrEqualTo(start.get("dateFrom"), to),
				exceeds(cb, root, unitsOn(busyStart, cb, root, start.get("dateFrom"), holding), units));

			return cb.and(cb.not(cb.exists(block)),
				cb.not(exceeds(cb, root, unitsOn(query, cb, root, cb.literal(from), holding), units)),
				cb.not(cb.exists(busyStart)));
		};
	}

	/** Which bookings hold units: CONFIRMED, or NEW made after {@code requestsAfter} and starting after {@code today}. */
	private record Holding(Instant requestsAfter, LocalDate today) {

		Predicate of(CriteriaBuilder cb, Root<Booking> b) {
			return cb.or(cb.equal(b.get("status"), BookingStatus.CONFIRMED),
				cb.and(cb.equal(b.get("status"), BookingStatus.NEW), cb.greaterThan(b.get("createdAt"), requestsAfter),
					cb.greaterThan(b.get("dateFrom"), today)));
		}
	}

	/** Sum of units of the listing's holding bookings covering {@code day}. */
	private static Subquery<Long> unitsOn(AbstractQuery<?> parent, CriteriaBuilder cb, Root<Equipment> root,
			Expression<LocalDate> day, Holding holding) {
		Subquery<Long> sum = parent.subquery(Long.class);
		Root<Booking> b = sum.from(Booking.class);
		sum.select(cb.sumAsLong(b.get("units"))).where(cb.equal(b.get("equipment"), root),
			holding.of(cb, b), cb.lessThanOrEqualTo(b.get("dateFrom"), day),
			cb.greaterThanOrEqualTo(b.get("dateTo"), day));
		return sum;
	}

	private static Predicate exceeds(CriteriaBuilder cb, Root<Equipment> root, Subquery<Long> taken, int units) {
		return cb.greaterThan(cb.sum(cb.coalesce(taken, 0L), (long) units),
			root.<Integer>get("quantity").as(Long.class));
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

	/**
	 * FR-7 rating sort: owners with published reviews first by their average (descending), owners without reviews
	 * last, ties newest first. Written as an explicit CASE because NULLS LAST is not portable through Spring's Sort and
	 * PostgreSQL sorts NULLs high. Like {@link #nearestFirst}, ordering is added to the data query only.
	 */
	public static Specification<Equipment> bestRatedFirst() {
		return (root, query, cb) -> {
			if (!Long.class.equals(query.getResultType())) {
				Expression<BigDecimal> rating = root.get("owner").get("ratingAvg");
				query.orderBy(cb.asc(cb.selectCase().when(cb.isNull(rating), 1).otherwise(0)), cb.desc(rating),
					cb.desc(root.get("createdAt")), cb.desc(root.get("id")));
			}
			return cb.conjunction();
		};
	}

	public static Specification<Equipment> notOwnedBy(Long userId) {
		return (root, query, cb) -> cb.notEqual(root.get("owner").get("id"), userId);
	}

	/**
	 * Recommendation order (D-44): nearest first, then categories in season in {@code month} (seasons may wrap over New
	 * Year), then owner rating (unrated last), then newest. Added to the data query only, like {@link #nearestFirst}.
	 */
	public static Specification<Equipment> nearestThenSeason(GeoPoint point, int month) {
		return (root, query, cb) -> {
			if (!Long.class.equals(query.getResultType())) {
				Expression<Integer> from = root.get("category").get("seasonFrom");
				Expression<Integer> to = root.get("category").get("seasonTo");
				Predicate inSeason = cb.or(
					cb.and(cb.le(from, to), cb.le(from, month), cb.ge(to, month)),
					cb.and(cb.gt(from, to), cb.or(cb.le(from, month), cb.ge(to, month))));
				Expression<BigDecimal> rating = root.get("owner").get("ratingAvg");
				query.orderBy(cb.asc(distanceKm(root, cb, point)),
					cb.asc(cb.selectCase().when(inSeason, 0).otherwise(1)),
					cb.asc(cb.selectCase().when(cb.isNull(rating), 1).otherwise(0)), cb.desc(rating),
					cb.desc(root.get("createdAt")), cb.desc(root.get("id")));
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
