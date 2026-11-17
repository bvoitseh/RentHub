package com.bohakka.renthub.service;

import com.bohakka.renthub.RenthubApplication;
import com.bohakka.renthub.config.AppProperties;
import com.bohakka.renthub.dto.EmailMessage;
import com.bohakka.renthub.event.PasswordResetEmailRequested;
import com.bohakka.renthub.event.VerificationEmailRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends account e-mails after the publishing transaction commits, on the async pool, so a slow or dead SMTP
 * server never blocks or fails the request. Texts come from messages_uk.properties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountMailListener {

	private final MailGateway mail;
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

	private void send(String to, String keyPrefix, Object... bodyArgs) {
		try {
			mail.send(new EmailMessage(to, text(keyPrefix + ".subject"), text(keyPrefix + ".body", bodyArgs)));
		} catch (RuntimeException ex) {
			log.error("Failed to send '{}' e-mail to {}", keyPrefix, to, ex);
		}
	}

	private String text(String key, Object... args) {
		return messages.getMessage(key, args, RenthubApplication.UI_LOCALE);
	}

	private String link(String path, String token) {
		return app.baseUrl() + path + "?token=" + token;
	}
}
