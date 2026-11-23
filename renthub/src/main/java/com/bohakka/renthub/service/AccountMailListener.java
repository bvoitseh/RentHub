package com.bohakka.renthub.service;

import com.bohakka.renthub.RenthubApplication;
import com.bohakka.renthub.properties.AppProperties;
import com.bohakka.renthub.dto.EmailMessage;
import com.bohakka.renthub.event.PasswordResetEmailRequested;
import com.bohakka.renthub.event.VerificationEmailRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends account e-mails after the publishing transaction commits, on the async pool, so a slow or dead SMTP
 * server never blocks or fails the request; failed sends are retried by {@link MailDelivery}. Texts come from
 * messages_uk.properties.
 */
@Component
@RequiredArgsConstructor
public class AccountMailListener {

	private final MailDelivery delivery;
	private final MessageSource messages;
	private final AppProperties app;

	@Async
	@TransactionalEventListener
	public void onVerificationRequested(VerificationEmailRequested event) {
		send(event.email(), "mail.verify", event.name(), link("/verify-email", event.rawToken()));
	}

	@Async
	@TransactionalEventListener
	public void onPasswordResetRequested(PasswordResetEmailRequested event) {
		send(event.email(), "mail.reset", event.name(), link("/reset-password", event.rawToken()));
	}

	/** Retries and failure logging are the delivery's job (D-41); logs name the template, never the address. */
	private void send(String to, String keyPrefix, Object... bodyArgs) {
		delivery.deliver(new EmailMessage(to, text(keyPrefix + ".subject"), text(keyPrefix + ".body", bodyArgs)),
			keyPrefix);
	}

	private String text(String key, Object... args) {
		return messages.getMessage(key, args, RenthubApplication.UI_LOCALE);
	}

	private String link(String path, String token) {
		return app.baseUrl() + path + "?token=" + token;
	}
}
