package com.bohakka.renthub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A listing photo (FR-4a): the database keeps only the object-storage key; position 0 is the cover photo. */
@Entity
@Table(name = "equipment_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentPhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "equipment_id", nullable = false)
	private Equipment equipment;

	@Column(name = "storage_key", nullable = false, unique = true, length = 200)
	private String storageKey;

	@Column(nullable = false)
	private int position;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	EquipmentPhoto(Equipment equipment, String storageKey, int position, Instant now) {
		this.equipment = equipment;
		this.storageKey = storageKey;
		this.position = position;
		this.createdAt = now;
	}

	void moveTo(int newPosition) {
		this.position = newPosition;
	}
}
