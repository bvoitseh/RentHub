package com.bohakka.renthub.entity;

/** ACTIVE listings are shown in the catalogue; ARCHIVED (by the owner, FR-5) and BLOCKED (by the admin, FR-20) are not. */
public enum EquipmentStatus {
	ACTIVE, ARCHIVED, BLOCKED
}
