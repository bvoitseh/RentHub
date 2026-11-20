package com.bohakka.renthub.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * FR-4a: S3-compatible object storage ({@code app.storage.*}, S3_* variables). {@code publicUrl} is the base under
 * which stored objects are publicly readable; photo URLs are {@code publicUrl + "/" + key}.
 */
@Validated
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(@NotBlank String endpoint, @NotBlank String region, @NotBlank String bucket,
		@NotBlank String accessKey, @NotBlank String secretKey, @NotBlank String publicUrl) {
}
