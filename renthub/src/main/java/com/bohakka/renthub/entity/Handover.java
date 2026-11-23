package com.bohakka.renthub.entity;

/** How the renter receives the equipment (FR-4b); it must be one of the options the listing offers. */
public enum Handover {
	PICKUP,
	DELIVERY;

	public boolean offeredBy(RentalTerms terms) {
		return this == PICKUP ? terms.pickupAvailable() : terms.deliveryAvailable();
	}
}
