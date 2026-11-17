package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.exception.RateLimitedException;
import com.bohakka.renthub.properties.RateLimitProperties.Policy;
import com.bohakka.renthub.service.RateLimiter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** In-memory sliding-window {@link RateLimiter}; a single instance is enough for this deployment. */
@Component
@RequiredArgsConstructor
public class RateLimiterImpl implements RateLimiter {

	private static final Duration IDLE_EVICTION = Duration.ofHours(2);

	private final Clock clock;
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	@Override
	public void assertAllowed(String scope, String value) {
		Bucket bucket = buckets.get(key(scope, value));
		if (bucket == null) {
			return;
		}
		Instant now = clock.instant();
		synchronized (bucket) {
			if (bucket.lockedUntil != null && bucket.lockedUntil.isAfter(now)) {
				long minutes = Math.max(1, Duration.between(now, bucket.lockedUntil).toMinutes());
				throw new RateLimitedException(minutes);
			}
		}
	}

	@Override
	public void record(String scope, String value, Policy policy) {
		Instant now = clock.instant();
		Bucket bucket = buckets.computeIfAbsent(key(scope, value), k -> new Bucket());
		synchronized (bucket) {
			bucket.lastHit = now;
			Instant windowStart = now.minus(policy.window());
			while (!bucket.hits.isEmpty() && bucket.hits.peekFirst().isBefore(windowStart)) {
				bucket.hits.pollFirst();
			}
			bucket.hits.addLast(now);
			if (bucket.hits.size() >= policy.limit()) {
				bucket.lockedUntil = now.plus(policy.window());
				bucket.hits.clear();
			}
		}
	}

	@Override
	public void consume(String scope, String value, Policy policy) {
		assertAllowed(scope, value);
		record(scope, value, policy);
	}

	@Override
	public void clear(String scope, String value) {
		buckets.remove(key(scope, value));
	}

	@Scheduled(fixedDelay = 600_000)
	void evictIdleBuckets() {
		Instant threshold = clock.instant().minus(IDLE_EVICTION);
		buckets.entrySet().removeIf(entry -> {
			Bucket bucket = entry.getValue();
			synchronized (bucket) {
				boolean lockOver = bucket.lockedUntil == null || bucket.lockedUntil.isBefore(threshold);
				return lockOver && bucket.lastHit.isBefore(threshold);
			}
		});
	}

	private static String key(String scope, String value) {
		return scope + ":" + value;
	}

	private static final class Bucket {
		private final Deque<Instant> hits = new ArrayDeque<>();
		private Instant lockedUntil;
		private Instant lastHit = Instant.EPOCH;
	}
}
