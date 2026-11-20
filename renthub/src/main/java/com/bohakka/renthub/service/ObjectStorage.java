package com.bohakka.renthub.service;

/** FR-4a: S3-compatible object storage for uploaded files; objects are publicly readable by URL. */
public interface ObjectStorage {

	void put(String key, byte[] content, String contentType);

	void delete(String key);

	String publicUrl(String key);
}
