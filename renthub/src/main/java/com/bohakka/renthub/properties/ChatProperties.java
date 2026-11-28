package com.bohakka.renthub.properties;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Chat settings bound from {@code app.chat.*} (D-57).
 *
 * @param notifyAfter how long a message may stay unread before its recipient is e-mailed (5 minutes)
 */
@Validated
@ConfigurationProperties(prefix = "app.chat")
public record ChatProperties(@NotNull Duration notifyAfter) {
}
