package com.bohakka.renthub.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Enables {@code @Async} (used for e-mail side effects triggered by application events)
 * and {@code @Scheduled} (booking completion/expiry, review publishing, chat e-mails). Pool sizes: spring.task.* in
 * application.yml. Both pools are declared explicitly: the WebSocket broker (D-57) registers its own executors and
 * scheduler, which would otherwise make Spring Boot skip the configured ones.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

	/** {@code @Async} picks the bean named "taskExecutor" when several executors exist. */
	@Bean(name = {"applicationTaskExecutor", "taskExecutor"})
	public ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
		return builder.build();
	}

	/** {@code @Scheduled} picks the bean named "taskScheduler" when several schedulers exist. */
	@Bean
	public ThreadPoolTaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder builder) {
		return builder.build();
	}
}
