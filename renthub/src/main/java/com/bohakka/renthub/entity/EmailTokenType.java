package com.bohakka.renthub.entity;

import java.time.Duration;

/** Kinds of single-use e-mail links and how long they stay valid (FR-1: 24 h, FR-3a: 1 h). */
public enum EmailTokenType {
	VERIFY_EMAIL(Duration.ofHours(24)),
	RESET_PASSWORD(Duration.ofHours(1));

	private final Duration validity;

	EmailTokenType(Duration validity) {
		this.validity = validity;
	}

	public Duration validity() {
		return validity;
	}
}
