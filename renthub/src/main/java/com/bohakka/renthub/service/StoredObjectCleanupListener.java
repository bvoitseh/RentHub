package com.bohakka.renthub.service;

import com.bohakka.renthub.event.StoredObjectUploaded;
import com.bohakka.renthub.event.StoredObjectsRemoved;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Keeps object storage in step with the database (FR-4a): files of deleted photos are removed after the commit,
 * and a file uploaded in a transaction that rolled back is removed as an orphan. Failures are only logged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoredObjectCleanupListener {

	private final ObjectStorage storage;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onRemoved(StoredObjectsRemoved event) {
		event.keys().forEach(this::deleteQuietly);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void onUploadRolledBack(StoredObjectUploaded event) {
		deleteQuietly(event.key());
	}

	private void deleteQuietly(String key) {
		try {
			storage.delete(key);
		} catch (RuntimeException ex) {
			log.warn("Failed to delete stored object {}", key, ex);
		}
	}
}
