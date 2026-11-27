package com.bohakka.renthub.service.impl;

import java.util.Arrays;
import java.util.Optional;

/**
 * FR-4a (D-52): accepted video formats, recognised by their leading bytes rather than by the client's Content-Type:
 * MP4 (an ISO base media file: "ftyp" box at offset 4) and WebM (EBML header).
 */
enum VideoFormat {
	MP4("video/mp4", "mp4"),
	WEBM("video/webm", "webm");

	static final int HEADER_BYTES = 12;
	private static final byte[] FTYP = {0x66, 0x74, 0x79, 0x70};
	private static final byte[] EBML = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};

	private final String contentType;
	private final String extension;

	VideoFormat(String contentType, String extension) {
		this.contentType = contentType;
		this.extension = extension;
	}

	String contentType() {
		return contentType;
	}

	String extension() {
		return extension;
	}

	static Optional<VideoFormat> detect(byte[] header) {
		if (startsWith(header, 4, FTYP)) {
			return Optional.of(MP4);
		}
		if (startsWith(header, 0, EBML)) {
			return Optional.of(WEBM);
		}
		return Optional.empty();
	}

	private static boolean startsWith(byte[] content, int offset, byte[] magic) {
		return content.length >= offset + magic.length
			&& Arrays.equals(content, offset, offset + magic.length, magic, 0, magic.length);
	}
}
