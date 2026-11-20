package com.bohakka.renthub.config;

import com.bohakka.renthub.properties.GeocodingProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** HTTP client for Nominatim: base URL and the identifying User-Agent from configuration, short timeouts. */
@Configuration
public class GeocodingConfig {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	@Bean
	public RestClient nominatimRestClient(GeocodingProperties properties) {
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
			HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
		requestFactory.setReadTimeout(TIMEOUT);
		return RestClient.builder()
			.baseUrl(properties.url())
			.defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
			.requestFactory(requestFactory)
			.build();
	}
}
