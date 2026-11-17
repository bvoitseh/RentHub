package com.bohakka.renthub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Platform-wide settings, a singleton row (id = 1): the commission rate in percent (FR-10, FR-22).
 * The row is created by migration V1 and only ever updated.
 */
@Entity
@Table(name = "settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settings {

	public static final int SINGLETON_ID = 1;
	private static final BigDecimal MAX_FEE_RATE = BigDecimal.valueOf(100);

	@Id
	private Integer id;

	@Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal feeRate;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public void changeFeeRate(BigDecimal newRate, Instant now) {
		if (newRate == null || newRate.signum() < 0 || newRate.compareTo(MAX_FEE_RATE) > 0) {
			throw new IllegalArgumentException("Fee rate must be between 0 and 100");
		}
		this.feeRate = newRate.setScale(2, java.math.RoundingMode.HALF_UP);
		this.updatedAt = now;
	}
}
