package com.bohakka.renthub.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * Attempts counted for one rate-limit key "{scope}:{value}" (NFR-14), e.g. "login:1.2.3.4". The window starts with the
 * first attempt: the TTL is set on creation, and Spring Data reads the remaining TTL back on load, so later saves keep it.
 */
@RedisHash("rate_limit_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RateLimitAttempts {

	@Id
	private String key;

	private int count;

	@TimeToLive
	private Long ttlSeconds;

	public RateLimitAttempts(String key, long windowSeconds) {
		this.key = key;
		this.ttlSeconds = windowSeconds;
	}

	public void increment() {
		count++;
	}
}
