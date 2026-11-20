package com.bohakka.renthub.service.impl;

import java.util.Arrays;
import java.util.Optional;

/**
 * FR-4a: accepted photo formats, recognised by their leading bytes rather than by the client's Content-Type, so a
 * renamed or mislabelled file is rejected.
 */
enum ImageFormat {
	JPEG("image/jpeg", "jpg"),
	PNG("image/png", "png"),
	WEBP("image/webp", "webp");

	private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
	private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	private static final byte[] RIFF = {0x52, 0x49, 0x46, 0x46};
	private static final byte[] WEBP_TAG = {0x57, 0x45, 0x42, 0x50};

	private final String contentType;
	private final String extension;

	ImageFormat(String contentType, String extension) {
		this.contentType = contentType;
		this.extension = extension;
	}

	String contentType() {
		return contentType;
	}

	String extension() {
		return extension;
	}

	static Optional<ImageFormat> detect(byte[] content) {
		if (startsWith(content, 0, JPEG_MAGIC)) {
			return Optional.of(JPEG);
		}
		if (startsWith(content, 0, PNG_MAGIC)) {
			return Optional.of(PNG);
		}
		if (startsWith(content, 0, RIFF) && startsWith(content, 8, WEBP_TAG)) {
			return Optional.of(WEBP);
		}
		return Optional.empty();
	}

	private static boolean startsWith(byte[] content, int offset, byte[] magic) {
		return content.length >= offset + magic.length
			&& Arrays.equals(content, offset, offset + magic.length, magic, 0, magic.length);
	}
}
