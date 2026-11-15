package com.bohakka.renthub.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application-level settings bound from {@code app.*} (see application.yml and .env.example).
 *
 * @param baseUrl  public URL of the SPA, used to build links in e-mails
 * @param timeZone business time zone ("today" is evaluated in it)
 * @param mailFrom sender address for outgoing e-mails
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@NotBlank String baseUrl, @NotBlank String timeZone, @NotBlank String mailFrom) {
}
