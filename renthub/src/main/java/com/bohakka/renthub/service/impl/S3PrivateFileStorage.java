package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.properties.StorageProperties;
import com.bohakka.renthub.service.PrivateFileStorage;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** {@link PrivateFileStorage} in the S3 chat bucket ({@code app.storage.chat-bucket}), which has no anonymous read. */
@Component
@RequiredArgsConstructor
public class S3PrivateFileStorage implements PrivateFileStorage {

	private final S3Client s3;
	private final StorageProperties properties;

	@Override
	public void put(String key, InputStream content, long size, String contentType) {
		s3.putObject(PutObjectRequest.builder().bucket(properties.chatBucket()).key(key).contentType(contentType).build(),
			RequestBody.fromInputStream(content, size));
	}

	@Override
	public InputStream read(String key, long start, long endInclusive) {
		return s3.getObject(GetObjectRequest.builder().bucket(properties.chatBucket()).key(key)
			.range("bytes=" + start + "-" + endInclusive).build());
	}

	@Override
	public void delete(String key) {
		s3.deleteObject(DeleteObjectRequest.builder().bucket(properties.chatBucket()).key(key).build());
	}
}
