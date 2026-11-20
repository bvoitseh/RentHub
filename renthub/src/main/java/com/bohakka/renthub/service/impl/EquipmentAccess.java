package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Ownership and permission checks shared by listing and photo management (NFR-4: enforced in the service layer). */
@Component
@RequiredArgsConstructor
public class EquipmentAccess {

	private final EquipmentRepository equipment;

	/** The listing with its details, if the caller owns it; 404 if it does not exist, 403 if it is someone else's. */
	public Equipment owned(Long ownerId, Long equipmentId) {
		Equipment listing = equipment.findWithDetailsById(equipmentId)
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
		if (!listing.isOwnedBy(ownerId)) {
			throw new ForbiddenException("equipment.not_owner", "equipment.not_owner");
		}
		return listing;
	}

	/** FR-1, FR-4: publishing needs an active user account with a verified e-mail; the admin does not rent out. */
	public void assertMayPublish(User user) {
		if (user.getRole() != Role.USER || !user.isActive()) {
			throw new ForbiddenException("equipment.publish_forbidden", "equipment.publish_forbidden");
		}
		if (!user.isEmailVerified()) {
			throw new ForbiddenException("user.email_not_verified", "user.email_not_verified");
		}
	}
}
