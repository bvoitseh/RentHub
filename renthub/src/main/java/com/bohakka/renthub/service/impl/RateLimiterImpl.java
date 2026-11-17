package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.RateLimitAttempts;
import com.bohakka.renthub.entity.RateLimitBlock;
import com.bohakka.renthub.exception.RateLimitedException;
import com.bohakka.renthub.properties.RateLimitProperties.Policy;
import com.bohakka.renthub.repository.RateLimitAttemptsRepository;
import com.bohakka.renthub.repository.RateLimitBlockRepository;
import com.bohakka.renthub.service.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link RateLimiter} over two Redis repositories: attempt counters that expire one window after the first attempt,
 * and blocks that expire one window after the limit was reached. Read-modify-write is not atomic, so concurrent
 * attempts on the same key may be under-counted by a few (DECISIONS.md D-29).
 */
@Component
@RequiredArgsConstructor
public class RateLimiterImpl implements RateLimiter {

	private final RateLimitAttemptsRepository attempts;
	private final RateLimitBlockRepository blocks;

	@Override
	public void assertAllowed(String scope, String value) {
		blocks.findById(key(scope, value)).ifPresent(block -> {
			long seconds = block.getTtlSeconds() == null ? 60 : block.getTtlSeconds();
			throw new RateLimitedException(Math.max(1, seconds / 60));
		});
	}

	@Override
	public void record(String scope, String value, Policy policy) {
		String key = key(scope, value);
		long windowSeconds = policy.window().toSeconds();
		RateLimitAttempts counter = attempts.findById(key)
			.filter(a -> a.getTtlSeconds() != null && a.getTtlSeconds() > 0)
			.orElseGet(() -> new RateLimitAttempts(key, windowSeconds));
		counter.increment();
		if (counter.getCount() >= policy.limit()) {
			blocks.save(new RateLimitBlock(key, windowSeconds));
			attempts.deleteById(key);
		} else {
			attempts.save(counter);
		}
	}

	@Override
	public void consume(String scope, String value, Policy policy) {
		assertAllowed(scope, value);
		record(scope, value, policy);
	}

	@Override
	public void clear(String scope, String value) {
		attempts.deleteById(key(scope, value));
	}

	private static String key(String scope, String value) {
		return scope + ":" + value;
	}
}
