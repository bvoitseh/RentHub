package com.bohakka.renthub.entity;

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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The chat between two users (D-57): one per pair, the smaller user id first. Each side has a read marker (the id of
 * the last message it has read) and a notified marker (the last message it was e-mailed about); a side is e-mailed
 * again only after it has read up to that point, so unread messages cause at most one e-mail.
 */
@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_low_id", nullable = false, updatable = false)
	private User userLow;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_high_id", nullable = false, updatable = false)
	private User userHigh;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "last_message_at")
	private Instant lastMessageAt;

	@Column(name = "low_last_read_id", nullable = false)
	private long lowLastReadId;

	@Column(name = "high_last_read_id", nullable = false)
	private long highLastReadId;

	@Column(name = "low_notified_id", nullable = false)
	private long lowNotifiedId;

	@Column(name = "high_notified_id", nullable = false)
	private long highNotifiedId;

	/** A new conversation between two different users, ordered by id. */
	public static Conversation between(User a, User b, Instant now) {
		Conversation c = new Conversation();
		boolean aFirst = a.getId() < b.getId();
		c.userLow = aFirst ? a : b;
		c.userHigh = aFirst ? b : a;
		c.createdAt = now;
		return c;
	}

	public boolean hasParticipant(Long userId) {
		return userLow.getId().equals(userId) || userHigh.getId().equals(userId);
	}

	/** Throws 403 unless the user takes part in this conversation. */
	public void requireParticipant(Long userId) {
		if (!hasParticipant(userId)) {
			throw new ForbiddenException("chat.not_participant", "chat.not_participant");
		}
	}

	public User other(Long userId) {
		requireParticipant(userId);
		return isLow(userId) ? userHigh : userLow;
	}

	public long lastReadId(Long userId) {
		requireParticipant(userId);
		return isLow(userId) ? lowLastReadId : highLastReadId;
	}

	public void messageSent(Instant at) {
		lastMessageAt = at;
	}

	/** Moves the side's read marker forward (never back). */
	public void markRead(Long userId, long messageId) {
		requireParticipant(userId);
		if (isLow(userId)) {
			lowLastReadId = Math.max(lowLastReadId, messageId);
		} else {
			highLastReadId = Math.max(highLastReadId, messageId);
		}
	}

	/** True when the side has read everything it was last e-mailed about, so a new e-mail may go out. */
	public boolean mayNotify(Long userId) {
		requireParticipant(userId);
		return isLow(userId) ? lowNotifiedId <= lowLastReadId : highNotifiedId <= highLastReadId;
	}

	public void notified(Long userId, long upToMessageId) {
		requireParticipant(userId);
		if (isLow(userId)) {
			lowNotifiedId = upToMessageId;
		} else {
			highNotifiedId = upToMessageId;
		}
	}

	private boolean isLow(Long userId) {
		return userLow.getId().equals(userId);
	}
}
