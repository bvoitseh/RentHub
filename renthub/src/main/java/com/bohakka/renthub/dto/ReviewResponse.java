package com.bohakka.renthub.dto;

import java.time.Instant;

/**
 * A review as the caller may see it (FR-18): published ones to everybody, an unpublished one only to its author.
 * {@code canReply} is true for the target while a reply is still allowed.
 */
public record ReviewResponse(Long id, Long bookingId, Item equipment, Person author, Person target, int rating,
		String comment, Instant createdAt, boolean published, Instant publishedAt, String reply, Instant repliedAt,
		boolean canReply) {

	public record Item(Long id, String title) {
	}

	/** {@code deactivated}: FR-3c, the profile and reviews show the user as deactivated. */
	public record Person(Long id, String name, boolean deactivated) {
	}
}
