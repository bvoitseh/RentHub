package com.bohakka.renthub.event;

/** Published right after a file is uploaded; if the transaction then rolls back, the orphaned file is removed. */
public record StoredObjectUploaded(String key) {
}
