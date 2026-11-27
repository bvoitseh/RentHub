package com.bohakka.renthub.service;

import java.io.InputStream;

/** FR-4a: S3-compatible object storage for uploaded files; objects are publicly readable by URL. */
public interface ObjectStorage {

	void put(String key, byte[] content, String contentType);

	/** Streams {@code size} bytes without holding the whole file in memory (videos, D-52). */
	void put(String key, InputStream content, long size, String contentType);

	void delete(String key);

	String publicUrl(String key);
}
