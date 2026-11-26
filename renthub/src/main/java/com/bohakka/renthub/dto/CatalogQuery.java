package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FR-7 catalogue query string: keywords, category, price range, rental dates, units, distance from a point,
 * handover options, owner type; {@code sort} is one of {@code price,asc}, {@code price,desc}, {@code distance},
 * {@code rating}, {@code newest}; when omitted: distance if lat/lon are given, otherwise price ascending (FR-7);
 * {@code page} is 0-based, {@code size} defaults to 20.
 */
public record CatalogQuery(
		@Size(max = 100) String q,
		Long categoryId,
		@PositiveOrZero BigDecimal priceMin,
		@PositiveOrZero BigDecimal priceMax,
		LocalDate dateFrom,
		LocalDate dateTo,
		@Min(1) @Max(1000) Integer units,
		@DecimalMin("-90") @DecimalMax("90") Double lat,
		@DecimalMin("-180") @DecimalMax("180") Double lon,
		@DecimalMin("0.1") @DecimalMax("500") Double radiusKm,
		Boolean pickup,
		Boolean delivery,
		OwnerType ownerType,
		String sort,
		@Min(0) Integer page,
		@Min(1) @Max(48) Integer size) {
}
