package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;

/** The caller's own account (GET /auth/me). The phone is the caller's own, so it may be shown (FR-3b). */
public record UserResponse(
		Long id,
		String name,
		String email,
		boolean emailVerified,
		String phone,
		Role role,
		String city,
		boolean geoConsent,
		OwnerType ownerType,
		String companyName,
		String edrpou) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.isEmailVerified(), user.getPhone(),
			user.getRole(), user.getCity(), user.isGeoConsent(), user.getOwnerType(), user.getCompanyName(),
			user.getEdrpou());
	}
}
