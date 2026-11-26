package com.bohakka.renthub.properties;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * FR-18 windows, bound from {@code app.review.*}.
 *
 * @param window      how long after completion each side may review (14 days)
 * @param replyWindow how long after publication the target may reply (14 days)
 */
@Validated
@ConfigurationProperties(prefix = "app.review")
public record ReviewProperties(@NotNull Duration window, @NotNull Duration replyWindow) {
}
