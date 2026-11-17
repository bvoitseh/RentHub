package com.bohakka.renthub.exception;

import org.springframework.http.HttpStatus;

/** 429: too many attempts; {@code retryAfterMinutes} tells the client when to try again. */
public class RateLimitedException extends DomainException {

	public RateLimitedException(long retryAfterMinutes) {
		super(HttpStatus.TOO_MANY_REQUESTS, "rate_limited", "error.rate_limited", retryAfterMinutes);
		withProperty("retryAfterMinutes", retryAfterMinutes);
	}
}
