package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.Settlement;
import com.bohakka.renthub.entity.SettlementStatus;

/** A settlement as suggested and as stored on a profile or listing (D-48); {@code label} disambiguates namesakes. */
public record SettlementResponse(Long id, String name, String label, SettlementStatus status, Long regionId,
		String regionName) {

	public static SettlementResponse from(Settlement s) {
		return s == null ? null : new SettlementResponse(s.getId(), s.getName(), s.getLabel(), s.getStatus(),
			s.getRegion().getId(), s.getRegion().getName());
	}
}
