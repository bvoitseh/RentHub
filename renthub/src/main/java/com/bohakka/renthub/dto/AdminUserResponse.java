package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import java.time.Instant;

/** FR-22: a user in the admin list; the admin may see contacts (FR-3b). */
public record AdminUserResponse(Long id, String name, String email, String phone, String city, OwnerType ownerType,
		boolean emailVerified, boolean blocked, boolean deactivated, long complaintCount, Instant createdAt) {
}
