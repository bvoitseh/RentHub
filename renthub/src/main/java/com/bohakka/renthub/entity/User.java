package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/** Account of a user (rents and rents out) or the admin (FR-1…FR-3c). Business rules about the account live here. */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified;

	@Column(length = 20)
	private String phone;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Role role;

	@Column(length = 100)
	private String city;

	@Column(name = "geo_consent", nullable = false)
	private boolean geoConsent;

	@Column(nullable = false)
	private boolean blocked;

	@Column(name = "deactivated_at")
	private Instant deactivatedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type", length = 10)
	private OwnerType ownerType;

	@Column(name = "company_name", length = 200)
	private String companyName;

	@Column(length = 8)
	private String edrpou;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	/** FR-19: sum and count of the ratings in published reviews about this user; the average is derived from them. */
	@Column(name = "rating_sum", nullable = false)
	private int ratingSum;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	/** Null while there are no published reviews (such users sort last by rating, FR-7). */
	@Column(name = "rating_avg", precision = 3, scale = 2)
	private BigDecimal ratingAvg;

	public User(String name, String email, String phone, String passwordHash, Role role, String city, Instant now) {
		this.name = name.trim();
		this.email = normalizeEmail(email);
		this.phone = phone;
		this.passwordHash = passwordHash;
		this.role = role;
		this.city = city;
		this.createdAt = now;
		this.ownerType = role == Role.USER ? OwnerType.PRIVATE : null;
	}

	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	/** FR-1a: users (not the admin) have an account type; a company must name itself and give an 8-digit ЄДРПОУ. */
	public void assignOwnerType(OwnerType type, String companyName, String edrpou) {
		if (role != Role.USER) {
			throw new BusinessRuleException("user.owner_type_not_for_admin", "user.owner_type_not_for_admin");
		}
		if (type == OwnerType.COMPANY && !(StringUtils.hasText(companyName) && StringUtils.hasText(edrpou))) {
			throw new BusinessRuleException("user.company_details_required", "user.company_details_required");
		}
		this.ownerType = type;
		this.companyName = type == OwnerType.COMPANY ? companyName.trim() : null;
		this.edrpou = type == OwnerType.COMPANY ? edrpou : null;
	}

	public void updateProfile(String name, String phone, String city) {
		this.name = name.trim();
		this.phone = phone;
		this.city = city;
	}

	public void markEmailVerified() {
		this.emailVerified = true;
	}

	public void changePassword(String newPasswordHash) {
		this.passwordHash = newPasswordHash;
	}

	public void setGeoConsent(boolean consent) {
		this.geoConsent = consent;
	}

	/** FR-3c: the account is kept, listings are hidden by the catalogue, no e-mails are sent. */
	public void deactivate(Instant now) {
		this.deactivatedAt = now;
	}

	public void restore() {
		this.deactivatedAt = null;
	}

	public boolean isDeactivated() {
		return deactivatedAt != null;
	}

	/**
	 * FR-19: counts a newly published review about this user. Callers hold this user's row lock, so concurrent
	 * publications cannot lose an update.
	 */
	public void addPublishedRating(int rating) {
		this.ratingSum += rating;
		this.reviewCount += 1;
		this.ratingAvg = BigDecimal.valueOf(ratingSum).divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);
	}

	/** FR-20: an admin block; the user cannot log in until unblocked. */
	public void block() {
		this.blocked = true;
	}

	public void unblock() {
		this.blocked = false;
	}

	/** Whether the account may log in and act (not blocked by an admin). */
	public boolean isActive() {
		return !blocked && !isDeactivated();
	}
}
