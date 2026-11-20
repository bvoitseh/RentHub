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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/** A period the owner closed for rental (FR-8); while it lasts, no unit of the listing is free (FR-9). */
@Entity
@Table(name = "availability_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailabilityBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "equipment_id", nullable = false, updatable = false)
	private Equipment equipment;

	@Column(name = "date_from", nullable = false, updatable = false)
	private LocalDate dateFrom;

	@Column(name = "date_to", nullable = false, updatable = false)
	private LocalDate dateTo;

	@Column(length = 200, updatable = false)
	private String reason;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public AvailabilityBlock(Equipment equipment, BookingPeriod period, String reason, Instant now) {
		this.equipment = equipment;
		this.dateFrom = period.from();
		this.dateTo = period.to();
		this.reason = StringUtils.hasText(reason) ? reason.trim() : null;
		this.createdAt = now;
	}

	public BookingPeriod period() {
		return new BookingPeriod(dateFrom, dateTo);
	}
}
