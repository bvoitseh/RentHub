package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.EmailMessage;

/** Application-owned boundary for outgoing e-mail (NFR-13); implemented over SMTP. */
public interface MailGateway {

	/** Sends the message synchronously; throws on transport failure so callers can log/retry. */
	void send(EmailMessage message);
}
