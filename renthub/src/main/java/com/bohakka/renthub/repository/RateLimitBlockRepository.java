package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.RateLimitBlock;
import org.springframework.data.repository.CrudRepository;

/** Redis repository of blocked rate-limit keys, keyed by "{scope}:{value}". */
public interface RateLimitBlockRepository extends CrudRepository<RateLimitBlock, String> {
}
