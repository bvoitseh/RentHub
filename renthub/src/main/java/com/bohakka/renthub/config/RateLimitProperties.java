package com.bohakka.renthub.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * NFR-14 limits, bound from {@code app.rate-limit.*}. Each policy allows {@code limit} hits per {@code window};
 * reaching the limit locks the key for another {@code window}.
 */
@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
		@NotNull @Valid Policy login,
		@NotNull @Valid Policy registration,
		@NotNull @Valid Policy passwordReset,
		@NotNull @Valid Policy resendVerification) {

	public record Policy(@Min(1) int limit, @NotNull Duration window) {
	}
}
