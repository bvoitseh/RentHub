package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.ComplaintStatus;
import java.time.Instant;

/** A complaint as its author or the admin sees it; {@code bookingId} or {@code equipment} names its subject. */
public record ComplaintResponse(Long id, Person author, Person against, Long bookingId, Item equipment,
		String description, ComplaintStatus status, Instant createdAt, Instant decidedAt) {

	public record Person(Long id, String name) {
	}

	public record Item(Long id, String title) {
	}
}
