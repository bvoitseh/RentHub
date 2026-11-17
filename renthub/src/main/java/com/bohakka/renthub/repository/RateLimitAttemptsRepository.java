package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.RateLimitAttempts;
import org.springframework.data.repository.CrudRepository;

/** Redis repository of rate-limit attempt counters, keyed by "{scope}:{value}". */
public interface RateLimitAttemptsRepository extends CrudRepository<RateLimitAttempts, String> {
}
