package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.properties.StorageProperties;
import com.bohakka.renthub.service.ObjectStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** {@link ObjectStorage} on any S3-compatible service (SeaweedFS locally) through the AWS SDK v2 client. */
@Component
@RequiredArgsConstructor
public class S3ObjectStorage implements ObjectStorage {

	private final S3Client s3;
	private final StorageProperties properties;

	@Override
	public void put(String key, byte[] content, String contentType) {
		s3.putObject(PutObjectRequest.builder().bucket(properties.bucket()).key(key).contentType(contentType).build(),
			RequestBody.fromBytes(content));
	}

	@Override
	public void put(String key, InputStream content, long size, String contentType) {
		s3.putObject(PutObjectRequest.builder().bucket(properties.bucket()).key(key).contentType(contentType).build(),
			RequestBody.fromInputStream(content, size));
	}

	@Override
	public void delete(String key) {
		s3.deleteObject(DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build());
	}

	@Override
	public String publicUrl(String key) {
		String base = properties.publicUrl();
		return (base.endsWith("/") ? base : base + "/") + key;
	}
}
