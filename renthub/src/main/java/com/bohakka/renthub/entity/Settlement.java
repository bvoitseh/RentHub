package com.bohakka.renthub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * A city, town or village of Ukraine (D-48): reference data generated from OpenStreetMap (name, district, community,
 * coordinates, postal code) and Wikidata (legal status). Inactive rows are hidden from search but stay referenced.
 */
@Entity
@Immutable
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

	@Id
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "region_id", nullable = false)
	private Region region;

	@Column(nullable = false)
	private String name;

	@Column(name = "search_name", nullable = false)
	private String searchName;

	@Enumerated(EnumType.STRING)
	private SettlementStatus status;

	private String district;

	private String community;

	/** Suggestion text, e.g. «с. Миколаївка, Лихівська громада, Кам'янський р-н». */
	@Column(nullable = false)
	private String label;

	@Column(nullable = false)
	private double lat;

	@Column(nullable = false)
	private double lon;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(nullable = false)
	private boolean active;

	public GeoPoint location() {
		return new GeoPoint(lat, lon);
	}

	/** Villages and селища usually have one postal code; cities have many (D-48, postal-code prefill). */
	public boolean hasSinglePostalCode() {
		return status == SettlementStatus.VILLAGE || status == SettlementStatus.TOWN;
	}

	/** The search key: lower case without apostrophes, as stored in settlements.search_name. */
	public static String searchKey(String text) {
		return text.replaceAll("[’'ʼ`]", "").toLowerCase(Locale.forLanguageTag("uk")).trim();
	}
}
