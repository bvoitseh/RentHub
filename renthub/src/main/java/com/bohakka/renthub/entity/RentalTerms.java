package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * Price and rental terms of a listing (FR-4b, FR-4c), validated as one unit: price > 0, quantity >= 1,
 * 1 <= minDays <= maxDays, pickup and/or delivery, a delivery note only when delivery is offered.
 */
public record RentalTerms(BigDecimal pricePerDay, int quantity, int minDays, Integer maxDays, boolean pickupAvailable,
		boolean deliveryAvailable, String deliveryNote) {

	public RentalTerms {
		if (pricePerDay == null || pricePerDay.signum() <= 0) {
			throw rule("equipment.price_invalid");
		}
		pricePerDay = pricePerDay.setScale(2, RoundingMode.HALF_UP);
		if (quantity < 1) {
			throw rule("equipment.quantity_invalid");
		}
		if (minDays < 1 || maxDays != null && maxDays < minDays) {
			throw rule("equipment.days_invalid");
		}
		if (!pickupAvailable && !deliveryAvailable) {
			throw rule("equipment.handover_required");
		}
		deliveryNote = deliveryAvailable && StringUtils.hasText(deliveryNote) ? deliveryNote.trim() : null;
	}

	/** Whether a rental lasting {@code days} days fits minDays/maxDays (FR-7, FR-10). */
	public boolean allowsDuration(int days) {
		return days >= minDays && (maxDays == null || days <= maxDays);
	}

	/**
	 * FR-5: changes that are frozen while the listing has active bookings: price, a smaller quantity, min/max days,
	 * handover options and the delivery note. A larger quantity is always allowed.
	 */
	public boolean differsInFrozenTerms(RentalTerms previous) {
		return pricePerDay.compareTo(previous.pricePerDay) != 0 || quantity < previous.quantity
			|| minDays != previous.minDays || !Objects.equals(maxDays, previous.maxDays)
			|| pickupAvailable != previous.pickupAvailable || deliveryAvailable != previous.deliveryAvailable
			|| !Objects.equals(deliveryNote, previous.deliveryNote);
	}

	private static BusinessRuleException rule(String key) {
		return new BusinessRuleException(key, key);
	}
}
