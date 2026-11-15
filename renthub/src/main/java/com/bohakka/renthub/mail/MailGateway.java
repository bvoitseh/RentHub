package com.bohakka.renthub.mail;

/** Application-owned boundary for outgoing e-mail (NFR-13); implemented over SMTP, swappable per profile. */
public interface MailGateway {

	/** Sends the message synchronously; throws on transport failure so callers can log/retry. */
	void send(EmailMessage message);
}
