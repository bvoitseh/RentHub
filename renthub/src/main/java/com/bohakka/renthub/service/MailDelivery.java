package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.EmailMessage;

/**
 * Best-effort delivery of an e-mail with retries (FR-15a, D-41). Blocks the calling thread while it waits between
 * attempts, so it is meant for the async mail listeners only; it never throws.
 */
public interface MailDelivery {

	/** {@code label} identifies the e-mail in the logs (e.g. "booking 42 CONFIRMED"); never put addresses in it. */
	void deliver(EmailMessage message, String label);
}
