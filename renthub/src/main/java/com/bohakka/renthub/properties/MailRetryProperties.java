package com.bohakka.renthub.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Retries of outgoing e-mail (FR-15a: "retried at most a few times"), bound from {@code app.mail.retry.*} (D-41).
 *
 * @param attempts total send attempts per e-mail (1 = no retry)
 * @param backoff  pauses before the 2nd, 3rd, … attempt; the last value repeats if there are more attempts
 */
@Validated
@ConfigurationProperties(prefix = "app.mail.retry")
public record MailRetryProperties(@Min(1) int attempts, @NotNull List<Duration> backoff) {
}
