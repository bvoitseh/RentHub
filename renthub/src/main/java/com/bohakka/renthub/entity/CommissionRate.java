package com.bohakka.renthub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One version of the platform commission (FR-10, FR-22): a percentage in force from {@code validFrom} until the next
 * version starts. Rows are never changed; a new rate is a new row, and each booking keeps the version it was priced
 * with (D-37).
 */
@Entity
@Table(name = "commission_rates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, precision = 5, scale = 2, updatable = false)
	private BigDecimal rate;

	@Column(name = "valid_from", nullable = false, updatable = false)
	private Instant validFrom;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	/** FR-22: a new version in force from {@code now}, for new booking requests only (D-46). */
	public CommissionRate(BigDecimal rate, Instant now) {
		this.rate = rate.setScale(2, RoundingMode.HALF_UP);
		this.validFrom = now;
		this.createdAt = now;
	}

	/** FR-10: commission = amount × rate / 100, HALF_UP to 2 decimals. */
	public BigDecimal commissionOn(BigDecimal amount) {
		return amount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}
}
