package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One side's review of the other after a completed rental (FR-18). Blind: hidden from the target and the public until
 * published ({@code publishedAt}); never edited after submission. The target may add one reply to a published review
 * within the reply window.
 */
@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

	public static final int TEXT_MAX = 1000;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "booking_id", nullable = false, updatable = false)
	private Booking booking;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false, updatable = false)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "target_id", nullable = false, updatable = false)
	private User target;

	@Column(nullable = false, updatable = false)
	private int rating;

	@Column(nullable = false, length = TEXT_MAX, updatable = false)
	private String comment;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(length = TEXT_MAX)
	private String reply;

	@Column(name = "replied_at")
	private Instant repliedAt;

	public Review(Booking booking, User author, User target, int rating, String comment, Instant now) {
		if (rating < 1 || rating > 5) {
			throw new BusinessRuleException("review.rating_invalid", "review.rating_invalid");
		}
		this.booking = booking;
		this.author = author;
		this.target = target;
		this.rating = rating;
		this.comment = text(comment, "review.comment_invalid");
		this.createdAt = now;
	}

	public boolean isPublished() {
		return publishedAt != null;
	}

	/** Blind rule: the author always sees the review, everyone else only once it is published. */
	public boolean isVisibleTo(Long userId) {
		return isPublished() || author.getId().equals(userId);
	}

	/** Publishes the review and counts it in the target's rating (FR-19); false if it was already published. */
	public boolean publish(Instant now) {
		if (isPublished()) {
			return false;
		}
		publishedAt = now;
		target.addPublishedRating(rating);
		return true;
	}

	/** Whether {@code userId} may still reply: the target, published, no reply yet, within the reply window. */
	public boolean canReply(Long userId, Instant now, Duration replyWindow) {
		return target.getId().equals(userId) && isPublished() && reply == null
			&& !now.isAfter(publishedAt.plus(replyWindow));
	}

	/** FR-18: one public reply by the target to a published review, within the window; replies are not edited. */
	public void reply(Long userId, String text, Instant now, Duration replyWindow) {
		if (!target.getId().equals(userId)) {
			throw new ForbiddenException("review.not_target", "review.not_target");
		}
		if (!isPublished()) {
			throw new ConflictException("review.not_published", "review.not_published");
		}
		if (reply != null) {
			throw new ConflictException("review.already_replied", "review.already_replied");
		}
		if (now.isAfter(publishedAt.plus(replyWindow))) {
			throw new BusinessRuleException("review.reply_window_closed", "review.reply_window_closed");
		}
		this.reply = text(text, "review.reply_invalid");
		this.repliedAt = now;
	}

	private static String text(String value, String errorKey) {
		String trimmed = value == null ? "" : value.trim();
		if (trimmed.isEmpty() || trimmed.length() > TEXT_MAX) {
			throw new BusinessRuleException(errorKey, errorKey, TEXT_MAX);
		}
		return trimmed;
	}
}
