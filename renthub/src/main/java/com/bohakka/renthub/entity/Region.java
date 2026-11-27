package com.bohakka.renthub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** A region of Ukraine (oblast, Crimea, Kyiv, Sevastopol); reference data from OpenStreetMap (D-48). */
@Entity
@Immutable
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

	@Id
	private Long id;

	@Column(name = "iso_code", nullable = false)
	private String isoCode;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private boolean active;

	/** Kyiv and Sevastopol are cities with the status of a region. */
	public boolean isCity() {
		return "UA-30".equals(isoCode) || "UA-40".equals(isoCode);
	}
}
