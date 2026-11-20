package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.GeocodeCacheEntry;
import org.springframework.data.repository.CrudRepository;

/** Redis repository of cached Nominatim answers, keyed by the normalised address query. */
public interface GeocodeCacheRepository extends CrudRepository<GeocodeCacheEntry, String> {
}
