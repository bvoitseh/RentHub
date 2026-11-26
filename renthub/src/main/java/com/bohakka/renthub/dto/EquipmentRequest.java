package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.ItemCondition;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Create or update a listing (FR-4, FR-4b, FR-4c). Cross-field rules (min/max days, at least one handover option)
 * are checked by the domain ({@code RentalTerms}); {@code lat}/{@code lon} is the pin the owner confirmed.
 */
public record EquipmentRequest(
		@NotNull Long categoryId,
		@NotBlank @Size(max = 120) String title,
		@NotBlank @Size(max = 4000) String description,
		@NotNull ItemCondition condition,
		@NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal pricePerDay,
		@NotNull @Min(1) @Max(1000) Integer quantity,
		@NotNull @Min(1) @Max(365) Integer minDays,
		@Min(1) @Max(365) Integer maxDays,
		boolean pickupAvailable,
		boolean deliveryAvailable,
		@Size(max = 500) String deliveryNote,
		@NotBlank @Size(max = 300) String address,
		@NotBlank @Size(max = 100) String city,
		@NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
		@NotNull @DecimalMin("-180") @DecimalMax("180") Double lon) {
}
