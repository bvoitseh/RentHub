package com.bohakka.renthub.config;

import com.bohakka.renthub.properties.StorageProperties;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/** S3 client for any S3-compatible storage (SeaweedFS locally, D-33); path-style addressing works with all of them. */
@Configuration
public class S3Config {

	@Bean(destroyMethod = "close")
	public S3Client s3Client(StorageProperties properties) {
		return S3Client.builder()
			.endpointOverride(URI.create(properties.endpoint()))
			.region(Region.of(properties.region()))
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
			.forcePathStyle(true)
			.build();
	}
}
