package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.NotFoundException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A listing (FR-4…FR-5). Owns its rental terms, location and photos (1-5, cover first), and its status rules.
 * Photos are added and removed only through this aggregate, which keeps their positions contiguous.
 */
@Entity
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment {

	/** FR-4a (D-52): 1-10 photos per listing. */
	public static final int MAX_PHOTOS = 10;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false, updatable = false)
	private User owner;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(nullable = false, length = 4000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "item_condition", nullable = false, length = 10)
	private ItemCondition condition;

	@Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
	private BigDecimal pricePerDay;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "min_days", nullable = false)
	private int minDays;

	@Column(name = "max_days")
	private Integer maxDays;

	@Column(name = "pickup_available", nullable = false)
	private boolean pickupAvailable;

	@Column(name = "delivery_available", nullable = false)
	private boolean deliveryAvailable;

	@Column(name = "delivery_note", length = 500)
	private String deliveryNote;

	@Column(nullable = false, length = 300)
	private String address;

	/** FR-23 E5: copy of the settlement name (D-46, D-48). */
	@Column(nullable = false, length = 100)
	private String city;

	/** FR-4: region + settlement of the listing (D-48); the address holds the street and house. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "settlement_id", nullable = false)
	private Settlement settlement;

	/** Optional five-digit postal code, prefilled from geocoding (D-48). */
	@Column(name = "postal_code", length = 5)
	private String postalCode;

	@Column(nullable = false)
	private double lat;

	@Column(nullable = false)
	private double lon;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private EquipmentStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	/** FR-4a (D-52): optional video, one per listing; key in the object storage. */
	@Column(name = "video_key", length = 200, unique = true)
	private String videoKey;

	@Column(name = "video_content_type", length = 20)
	private String videoContentType;

	@OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC")
	private List<EquipmentPhoto> photos = new ArrayList<>();

	public Equipment(User owner, Category category, String title, String description, ItemCondition condition,
			RentalTerms terms, ListingAddress location, Instant now) {
		this.owner = owner;
		this.status = EquipmentStatus.ACTIVE;
		this.createdAt = now;
		describe(category, title, description, condition, now);
		applyTerms(terms, now);
		relocate(location, now);
	}

	/** Descriptive fields (FR-5: always editable, even with active bookings). */
	public void describe(Category category, String title, String description, ItemCondition condition, Instant now) {
		this.category = category;
		this.title = title.trim();
		this.description = description.trim();
		this.condition = condition;
		this.updatedAt = now;
	}

	public void applyTerms(RentalTerms terms, Instant now) {
		this.pricePerDay = terms.pricePerDay();
		this.quantity = terms.quantity();
		this.minDays = terms.minDays();
		this.maxDays = terms.maxDays();
		this.pickupAvailable = terms.pickupAvailable();
		this.deliveryAvailable = terms.deliveryAvailable();
		this.deliveryNote = terms.deliveryNote();
		this.updatedAt = now;
	}

	/** FR-4: settlement, street and house, optional postal code and the pin the owner confirmed (D-48). */
	public void relocate(ListingAddress location, Instant now) {
		GeoPoint point = location.point();
		this.settlement = location.settlement();
		this.city = location.settlement().getName();
		this.address = location.street().trim();
		this.postalCode = location.postalCode();
		this.lat = point.lat();
		this.lon = point.lon();
		this.updatedAt = now;
	}

	public RentalTerms terms() {
		return new RentalTerms(pricePerDay, quantity, minDays, maxDays, pickupAvailable, deliveryAvailable, deliveryNote);
	}

	public GeoPoint location() {
		return new GeoPoint(lat, lon);
	}

	/** FR-6, FR-3c: shown in the catalogue and bookable — active, owner neither blocked nor deactivated, has a photo. */
	public boolean isPublic() {
		return status == EquipmentStatus.ACTIVE && owner.isActive() && !photos.isEmpty();
	}

	public boolean isOwnedBy(Long userId) {
		return owner.getId().equals(userId);
	}

	/** FR-22: the category is deleted and its listings move to {@code target} (D-46). */
	public void moveTo(Category target, Instant now) {
		this.category = target;
		this.updatedAt = now;
	}

	/** FR-20: an admin block hides the listing from the catalogue and stops new bookings. */
	public void block(Instant now) {
		this.status = EquipmentStatus.BLOCKED;
		this.updatedAt = now;
	}

	/** FR-20: unblocking makes the listing active again (D-45), even if it had been archived before. */
	public void unblock(Instant now) {
		if (status == EquipmentStatus.BLOCKED) {
			this.status = EquipmentStatus.ACTIVE;
			this.updatedAt = now;
		}
	}

	/** FR-5: the owner archives the listing; it leaves the catalogue. A blocked listing stays blocked. */
	public void archive(Instant now) {
		if (status == EquipmentStatus.BLOCKED) {
			throw new BusinessRuleException("equipment.blocked", "equipment.blocked");
		}
		this.status = EquipmentStatus.ARCHIVED;
		this.updatedAt = now;
	}

	public EquipmentPhoto addPhoto(String storageKey, Instant now) {
		if (photos.size() >= MAX_PHOTOS) {
			throw new BusinessRuleException("photo.limit_reached", "photo.limit_reached", MAX_PHOTOS);
		}
		EquipmentPhoto photo = new EquipmentPhoto(this, storageKey, photos.size(), now);
		photos.add(photo);
		this.updatedAt = now;
		return photo;
	}

	/** Removes the photo and returns its storage key; a listing keeps at least one photo (FR-4a: 1-10 photos). */
	public String removePhoto(Long photoId, Instant now) {
		EquipmentPhoto photo = photos.stream().filter(p -> p.getId().equals(photoId)).findFirst()
			.orElseThrow(() -> new NotFoundException("photo.not_found", "photo.not_found"));
		if (photos.size() == 1) {
			throw new BusinessRuleException("photo.last_one", "photo.last_one");
		}
		photos.remove(photo);
		for (int i = 0; i < photos.size(); i++) {
			photos.get(i).moveTo(i);
		}
		this.updatedAt = now;
		return photo.getStorageKey();
	}

	/**
	 * FR-4a (D-52): sets the listing's video; a new upload replaces the previous one, whose storage key is returned
	 * (or null) so the old file can be removed after the commit.
	 */
	public String replaceVideo(String storageKey, String contentType, Instant now) {
		String previous = videoKey;
		this.videoKey = storageKey;
		this.videoContentType = contentType;
		this.updatedAt = now;
		return previous;
	}

	/** Removes the video and returns its storage key. */
	public String removeVideo(Instant now) {
		if (videoKey == null) {
			throw new NotFoundException("video.not_found", "video.not_found");
		}
		String key = videoKey;
		this.videoKey = null;
		this.videoContentType = null;
		this.updatedAt = now;
		return key;
	}

	/** Every stored file of the listing (photos and the video), removed when the listing is deleted. */
	public List<String> storedKeys() {
		List<String> keys = new ArrayList<>(photos.stream().map(EquipmentPhoto::getStorageKey).toList());
		if (videoKey != null) {
			keys.add(videoKey);
		}
		return keys;
	}
}
