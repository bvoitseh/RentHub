package com.bohakka.renthub.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Single source of "now" for the whole application: inject {@link Clock}, never call {@code now()} statically. */
@Configuration
public class TimeConfig {

	@Bean
	public Clock clock(AppProperties properties) {
		return Clock.system(ZoneId.of(properties.timeZone()));
	}
}
