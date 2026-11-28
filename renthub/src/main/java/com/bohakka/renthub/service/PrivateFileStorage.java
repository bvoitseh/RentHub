package com.bohakka.renthub.service;

import java.io.InputStream;

/**
 * Storage for files that must not be publicly readable (chat attachments, D-57). Files are served only through the
 * backend after an access check; {@link #read} returns a byte range so audio and video can be seeked.
 */
public interface PrivateFileStorage {

	void put(String key, InputStream content, long size, String contentType);

	/** Bytes {@code start}…{@code endInclusive} of the file; the caller closes the stream. */
	InputStream read(String key, long start, long endInclusive);

	void delete(String key);
}
