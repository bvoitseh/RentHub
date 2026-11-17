package com.bohakka.renthub.service;

import com.bohakka.renthub.exception.RateLimitedException;
import com.bohakka.renthub.properties.RateLimitProperties.Policy;

/**
 * Rate limiting for sensitive endpoints (NFR-14). Keys are "{scope}:{value}", e.g. "login:1.2.3.4" or
 * "login:user@example.com"; a key that reaches {@code policy.limit()} hits within {@code policy.window()} is locked.
 */
public interface RateLimiter {

	/** Throws {@link RateLimitedException} when the key is currently locked. */
	void assertAllowed(String scope, String value);

	/** Counts one hit; locks the key once the limit is reached inside the window. */
	void record(String scope, String value, Policy policy);

	/** Checks and counts in one step, for endpoints where every call is an attempt (registration, reset). */
	void consume(String scope, String value, Policy policy);

	void clear(String scope, String value);
}
