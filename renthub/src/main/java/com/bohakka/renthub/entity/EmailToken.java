package com.bohakka.renthub.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * A single-use e-mail link token (NFR-5), stored in Redis as hash {@code email-token:{sha256}}. Only the SHA-256 of
 * the raw token is kept; the entry expires after {@link EmailTokenType#validity()} (Redis TTL).
 */
@RedisHash("email-token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailToken {

	@Id
	private String tokenHash;

	@Indexed
	private Long userId;

	@Indexed
	private EmailTokenType type;

	@TimeToLive
	private Long ttlSeconds;

	public EmailToken(String tokenHash, Long userId, EmailTokenType type) {
		this.tokenHash = tokenHash;
		this.userId = userId;
		this.type = type;
		this.ttlSeconds = type.validity().toSeconds();
	}
}
