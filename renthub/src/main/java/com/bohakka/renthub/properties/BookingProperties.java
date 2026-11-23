package com.bohakka.renthub.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Booking rules bound from {@code app.booking.*}.
 *
 * @param minLeadDays the earliest first day is today + this many days (spec: "no earlier than tomorrow")
 * @param horizonDays the last day may be at most today + this many days (D-38)
 * @param requestTtl  how long a NEW request waits for the owner before it expires (FR-15b)
 */
@Validated
@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(@Min(0) int minLeadDays, @Min(1) int horizonDays, @NotNull Duration requestTtl) {
}
