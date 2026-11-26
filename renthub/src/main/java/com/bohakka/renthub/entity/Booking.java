package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A booking request and its lifecycle (FR-10…FR-15b). Price, total and commission are fixed when the request is made.
 * Each transition method checks who may perform it and when; {@link BookingStatus} decides which status may follow.
 */
@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

	public static final int CANCEL_REASON_MIN = 10;
	public static final int CANCEL_REASON_MAX = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "equipment_id", nullable = false, updatable = false)
	private Equipment equipment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "renter_id", nullable = false, updatable = false)
	private User renter;

	@Column(nullable = false, updatable = false)
	private int units;

	@Column(name = "date_from", nullable = false, updatable = false)
	private LocalDate dateFrom;

	@Column(name = "date_to", nullable = false, updatable = false)
	private LocalDate dateTo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10, updatable = false)
	private Handover handover;

	@Column(name = "price_per_day", nullable = false, precision = 10, scale = 2, updatable = false)
	private BigDecimal pricePerDay;

	@Column(name = "total_price", nullable = false, precision = 12, scale = 2, updatable = false)
	private BigDecimal totalPrice;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "commission_rate_id", nullable = false, updatable = false)
	private CommissionRate commissionRate;

	@Column(nullable = false, precision = 12, scale = 2, updatable = false)
	private BigDecimal commission;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private BookingStatus status;

	@Column(nullable = false)
	private boolean expired;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "decided_at")
	private Instant decidedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "cancelled_by", length = 10)
	private CancelledBy cancelledBy;

	@Column(name = "cancel_reason", length = CANCEL_REASON_MAX)
	private String cancelReason;

	/** FR-10: totalPrice = days × units × pricePerDay; commission = totalPrice × rate / 100. */
	public Booking(Equipment equipment, User renter, int units, BookingPeriod period, Handover handover,
			CommissionRate rate, Instant now) {
		this.equipment = equipment;
		this.renter = renter;
		this.units = units;
		this.dateFrom = period.from();
		this.dateTo = period.to();
		this.handover = handover;
		this.pricePerDay = equipment.getPricePerDay();
		this.totalPrice = pricePerDay.multiply(BigDecimal.valueOf((long) period.days() * units));
		this.commissionRate = rate;
		this.commission = rate.commissionOn(totalPrice);
		this.status = BookingStatus.NEW;
		this.createdAt = now;
	}

	public BookingPeriod period() {
		return new BookingPeriod(dateFrom, dateTo);
	}

	public boolean isRenter(Long userId) {
		return renter.getId().equals(userId);
	}

	public boolean isOwner(Long userId) {
		return equipment.isOwnedBy(userId);
	}

	/** FR-12: the equipment owner accepts a request. */
	public void confirm(Long ownerId, Instant now) {
		requireOwner(ownerId);
		status = status.moveTo(BookingStatus.CONFIRMED);
		decidedAt = now;
	}

	/** FR-12: the equipment owner declines a request. */
	public void reject(Long ownerId, Instant now) {
		requireOwner(ownerId);
		requireStatus(BookingStatus.NEW);
		status = status.moveTo(BookingStatus.REJECTED);
		decidedAt = now;
	}

	/** FR-15b: an unanswered request runs out (no actor). */
	public void expire(Instant now) {
		requireStatus(BookingStatus.NEW);
		status = status.moveTo(BookingStatus.REJECTED);
		expired = true;
		decidedAt = now;
	}

	/** FR-13: the renter cancels a NEW or CONFIRMED booking before the rental starts. */
	public void cancelByRenter(Long renterId, LocalDate today, Instant now) {
		if (!isRenter(renterId)) {
			throw notParticipant();
		}
		cancel(CancelledBy.RENTER, null, today, now);
	}

	/** FR-13a: the owner cancels a CONFIRMED booking before the rental starts, with a reason of 10–500 characters. */
	public void cancelByOwner(Long ownerId, String reason, LocalDate today, Instant now) {
		requireOwner(ownerId);
		requireStatus(BookingStatus.CONFIRMED);
		String trimmed = reason == null ? "" : reason.trim();
		if (trimmed.length() < CANCEL_REASON_MIN || trimmed.length() > CANCEL_REASON_MAX) {
			throw new BusinessRuleException("booking.cancel_reason_required", "booking.cancel_reason_required",
				CANCEL_REASON_MIN, CANCEL_REASON_MAX);
		}
		cancel(CancelledBy.OWNER, trimmed, today, now);
	}

	/** FR-20: cancelled because the admin blocked a user or listing; only bookings that have not started. */
	public void cancelByAdmin(String reason, LocalDate today, Instant now) {
		cancel(CancelledBy.ADMIN, reason, today, now);
	}

	/** FR-15: a confirmed rental whose last day has passed. */
	public void complete(Instant now) {
		status = status.moveTo(BookingStatus.COMPLETED);
		completedAt = now;
	}

	private void cancel(CancelledBy by, String reason, LocalDate today, Instant now) {
		if (!today.isBefore(dateFrom)) {
			throw new BusinessRuleException("booking.already_started", "booking.already_started");
		}
		status = status.moveTo(BookingStatus.CANCELLED);
		cancelledBy = by;
		cancelReason = reason;
		decidedAt = now;
	}

	private void requireOwner(Long userId) {
		if (!isOwner(userId)) {
			throw notParticipant();
		}
	}

	private void requireStatus(BookingStatus expected) {
		if (status != expected) {
			throw new ConflictException("booking.transition_invalid", "booking.transition_invalid");
		}
	}

	private static ForbiddenException notParticipant() {
		return new ForbiddenException("booking.not_participant", "booking.not_participant");
	}
}
