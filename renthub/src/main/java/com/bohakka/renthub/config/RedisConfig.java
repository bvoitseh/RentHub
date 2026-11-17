package com.bohakka.renthub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * Redis repositories (e-mail link tokens). Keyspace events are enabled so that when Redis expires a token by its TTL,
 * Spring Data also removes it from the secondary indexes ({@code userId}, {@code type}).
 */
@Configuration
@EnableRedisRepositories(basePackages = "com.bohakka.renthub.repository",
	enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {
}
