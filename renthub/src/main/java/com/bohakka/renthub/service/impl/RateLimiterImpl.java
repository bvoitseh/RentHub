package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.exception.RateLimitedException;
import com.bohakka.renthub.properties.RateLimitProperties.Policy;
import com.bohakka.renthub.service.RateLimiter;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis-backed sliding-window {@link RateLimiter}. Per key, hits are a sorted set scored by time
 * ({@code rate-limit:hits:{scope}:{value}}); reaching the limit sets a lock key ({@code rate-limit:lock:...}) that
 * expires after one window. Every key carries a TTL, so Redis cleans up idle keys itself.
 */
@Component
@RequiredArgsConstructor
public class RateLimiterImpl implements RateLimiter {

	/** Atomically drops hits older than the window, adds this hit and locks the key once the limit is reached. */
	private static final RedisScript<Long> RECORD_HIT = RedisScript.of("""
		local hits, lock = KEYS[1], KEYS[2]
		local now, window, limit = tonumber(ARGV[1]), tonumber(ARGV[2]), tonumber(ARGV[3])
		redis.call('ZREMRANGEBYSCORE', hits, '-inf', now - window)
		redis.call('ZADD', hits, now, ARGV[4])
		if redis.call('ZCARD', hits) >= limit then
			redis.call('SET', lock, '1', 'PX', window)
			redis.call('DEL', hits)
			return 1
		end
		redis.call('PEXPIRE', hits, window)
		return 0
		""", Long.class);

	private final StringRedisTemplate redis;
	private final Clock clock;

	@Override
	public void assertAllowed(String scope, String value) {
		Long remainingMs = redis.getExpire(lockKey(scope, value), TimeUnit.MILLISECONDS);
		if (remainingMs != null && remainingMs > 0) {
			throw new RateLimitedException(Math.max(1, TimeUnit.MILLISECONDS.toMinutes(remainingMs)));
		}
	}

	@Override
	public void record(String scope, String value, Policy policy) {
		redis.execute(RECORD_HIT, List.of(hitsKey(scope, value), lockKey(scope, value)),
			String.valueOf(clock.millis()), String.valueOf(policy.window().toMillis()), String.valueOf(policy.limit()),
			UUID.randomUUID().toString());
	}

	@Override
	public void consume(String scope, String value, Policy policy) {
		assertAllowed(scope, value);
		record(scope, value, policy);
	}

	@Override
	public void clear(String scope, String value) {
		redis.delete(List.of(hitsKey(scope, value), lockKey(scope, value)));
	}

	private static String hitsKey(String scope, String value) {
		return "rate-limit:hits:" + scope + ":" + value;
	}

	private static String lockKey(String scope, String value) {
		return "rate-limit:lock:" + scope + ":" + value;
	}
}
