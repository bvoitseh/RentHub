package com.bohakka.renthub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Async} (used for e-mail side effects triggered by application events)
 * and {@code @Scheduled} (booking completion/expiry, review publishing). Pool sizes: spring.task.* in application.yml.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
