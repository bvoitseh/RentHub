package com.bohakka.renthub.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/** A rate-limit key that reached its limit (NFR-14); it is blocked until the entry expires. */
@RedisHash("rate_limit_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RateLimitBlock {

	@Id
	private String key;

	/** On load: the remaining block time, read back from Redis. */
	@TimeToLive
	private Long ttlSeconds;

	public RateLimitBlock(String key, long blockSeconds) {
		this.key = key;
		this.ttlSeconds = blockSeconds;
	}
}
