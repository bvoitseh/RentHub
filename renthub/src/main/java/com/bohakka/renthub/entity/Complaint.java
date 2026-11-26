package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A complaint (FR-21) by {@code author} against {@code againstUser}, about exactly one booking or one listing, with a
 * description of 10–1000 characters. It is NEW until the admin resolves or rejects it (FR-22); a rejected complaint
 * is unfounded and does not count on the user's profile.
 */
@Entity
@Table(name = "complaints")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Complaint {

	public static final int DESCRIPTION_MIN = 10;
	public static final int DESCRIPTION_MAX = 1000;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false, updatable = false)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "against_user_id", nullable = false, updatable = false)
	private User againstUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", updatable = false)
	private Booking booking;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "equipment_id", updatable = false)
	private Equipment equipment;

	@Column(nullable = false, length = DESCRIPTION_MAX, updatable = false)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private ComplaintStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "decided_at")
	private Instant decidedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "decided_by")
	private User decidedBy;

	private Complaint(User author, User againstUser, Booking booking, Equipment equipment, String description,
			Instant now) {
		String text = description == null ? "" : description.trim();
		if (text.length() < DESCRIPTION_MIN || text.length() > DESCRIPTION_MAX) {
			throw new BusinessRuleException("complaint.description_invalid", "complaint.description_invalid",
				DESCRIPTION_MIN, DESCRIPTION_MAX);
		}
		this.author = author;
		this.againstUser = againstUser;
		this.booking = booking;
		this.equipment = equipment;
		this.description = text;
		this.status = ComplaintStatus.NEW;
		this.createdAt = now;
	}

	/** About a booking: against the other side of it. */
	public static Complaint aboutBooking(User author, Booking booking, User other, String description, Instant now) {
		return new Complaint(author, other, booking, null, description, now);
	}

	/** About a listing: against its owner. */
	public static Complaint aboutListing(User author, Equipment listing, String description, Instant now) {
		return new Complaint(author, listing.getOwner(), null, listing, description, now);
	}

	public void resolve(User admin, Instant now) {
		decide(ComplaintStatus.RESOLVED, admin, now);
	}

	/** Rejected as unfounded: no longer counted on the profile (FR-21). */
	public void reject(User admin, Instant now) {
		decide(ComplaintStatus.REJECTED, admin, now);
	}

	private void decide(ComplaintStatus result, User admin, Instant now) {
		if (status != ComplaintStatus.NEW) {
			throw new ConflictException("complaint.already_decided", "complaint.already_decided");
		}
		this.status = result;
		this.decidedBy = admin;
		this.decidedAt = now;
	}
}
