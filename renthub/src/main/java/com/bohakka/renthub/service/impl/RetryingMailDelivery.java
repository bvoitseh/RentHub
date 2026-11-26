package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.EmailMessage;
import com.bohakka.renthub.properties.MailRetryProperties;
import com.bohakka.renthub.service.MailDelivery;
import com.bohakka.renthub.service.MailGateway;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@link MailDelivery} over {@link MailGateway}: up to {@code app.mail.retry.attempts} attempts with the configured
 * pauses; each failed attempt is logged at WARN, giving up at ERROR. Pending retries live only in memory, so a restart
 * drops them (D-41). Logs carry the label, never the recipient address.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryingMailDelivery implements MailDelivery {

	private final MailGateway gateway;
	private final MailRetryProperties retry;

	@Override
	public void deliver(EmailMessage message, String label) {
		for (int attempt = 1; attempt <= retry.attempts(); attempt++) {
			try {
				gateway.send(message);
				log.debug("Sent e-mail '{}' (attempt {})", label, attempt);
				return;
			} catch (RuntimeException ex) {
				if (attempt == retry.attempts()) {
					log.error("Giving up on e-mail '{}' after {} attempt(s): {}", label, attempt, ex.toString());
					return;
				}
				Duration pause = pauseBefore(attempt + 1);
				log.warn("E-mail '{}' failed (attempt {}), retrying in {}s: {}", label, attempt, pause.toSeconds(),
					ex.toString());
				if (!sleep(pause)) {
					return;
				}
			}
		}
	}

	private Duration pauseBefore(int attempt) {
		List<Duration> backoff = retry.backoff();
		if (backoff.isEmpty()) {
			return Duration.ZERO;
		}
		return backoff.get(Math.min(attempt - 2, backoff.size() - 1));
	}

	private static boolean sleep(Duration pause) {
		try {
			Thread.sleep(pause);
			return true;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		}
	}
}
