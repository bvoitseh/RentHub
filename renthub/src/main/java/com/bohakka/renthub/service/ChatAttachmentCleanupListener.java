package com.bohakka.renthub.service;

import com.bohakka.renthub.event.ChatAttachmentUploaded;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Removes a chat attachment whose message was not saved because the transaction rolled back (D-57). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAttachmentCleanupListener {

	private final PrivateFileStorage storage;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void onUploadRolledBack(ChatAttachmentUploaded event) {
		try {
			storage.delete(event.key());
		} catch (RuntimeException ex) {
			log.warn("Failed to delete chat attachment {}", event.key(), ex);
		}
	}
}
