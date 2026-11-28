package com.bohakka.renthub.service.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Chat attachment formats (D-57), recognised by their leading bytes rather than the client's Content-Type. ZIP-based
 * (OOXML, OpenDocument, ZIP) and OLE-based (legacy Office) files share a signature, so the extension picks the
 * format among those; plain text is accepted only as {@code .txt} without NUL bytes in the sniffed header.
 */
enum AttachmentFormat {
	JPEG("image/jpeg", "jpg", true),
	PNG("image/png", "png", true),
	WEBP("image/webp", "webp", true),
	GIF("image/gif", "gif", true),
	PDF("application/pdf", "pdf", true),
	DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", false),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", false),
	PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx", false),
	ODT("application/vnd.oasis.opendocument.text", "odt", false),
	ODS("application/vnd.oasis.opendocument.spreadsheet", "ods", false),
	ODP("application/vnd.oasis.opendocument.presentation", "odp", false),
	ZIP("application/zip", "zip", false),
	DOC("application/msword", "doc", false),
	XLS("application/vnd.ms-excel", "xls", false),
	PPT("application/vnd.ms-powerpoint", "ppt", false),
	TXT("text/plain; charset=UTF-8", "txt", false),
	MP4("video/mp4", "mp4", true),
	WEBM("video/webm", "webm", true),
	OGG("audio/ogg", "ogg", true);

	static final int HEADER_BYTES = 8192;
	static final Set<AttachmentFormat> DOCUMENTS = Set.of(JPEG, PNG, WEBP, GIF, PDF, DOCX, XLSX, PPTX, ODT, ODS, ODP,
		ZIP, DOC, XLS, PPT, TXT);
	static final Set<AttachmentFormat> VIDEOS = Set.of(MP4, WEBM);
	static final Set<AttachmentFormat> AUDIO = Set.of(WEBM, OGG, MP4);

	private static final Set<AttachmentFormat> ZIP_BASED = Set.of(DOCX, XLSX, PPTX, ODT, ODS, ODP, ZIP);
	private static final Set<AttachmentFormat> OLE_BASED = Set.of(DOC, XLS, PPT);
	private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
	private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	private static final byte[] RIFF = {0x52, 0x49, 0x46, 0x46};
	private static final byte[] WEBP_TAG = {0x57, 0x45, 0x42, 0x50};
	private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46, 0x38};
	private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};
	private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};
	private static final byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1,
		0x1A, (byte) 0xE1};
	private static final byte[] FTYP = {0x66, 0x74, 0x79, 0x70};
	private static final byte[] EBML = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};
	private static final byte[] OGG_MAGIC = {0x4F, 0x67, 0x67, 0x53};

	private final String contentType;
	private final String extension;
	private final boolean inline;

	AttachmentFormat(String contentType, String extension, boolean inline) {
		this.contentType = contentType;
		this.extension = extension;
		this.inline = inline;
	}

	String contentType() {
		return contentType;
	}

	String extension() {
		return extension;
	}

	/** Whether the browser may show it in place (images, PDF, media); everything else is a download. */
	boolean inline() {
		return inline;
	}

	/** {@code extension} is the lower-case extension of the original file name, or "" without one. */
	static Optional<AttachmentFormat> detect(byte[] header, String extension) {
		if (startsWith(header, 0, JPEG_MAGIC)) {
			return Optional.of(JPEG);
		}
		if (startsWith(header, 0, PNG_MAGIC)) {
			return Optional.of(PNG);
		}
		if (startsWith(header, 0, RIFF) && startsWith(header, 8, WEBP_TAG)) {
			return Optional.of(WEBP);
		}
		if (startsWith(header, 0, GIF_MAGIC)) {
			return Optional.of(GIF);
		}
		if (startsWith(header, 0, PDF_MAGIC)) {
			return Optional.of(PDF);
		}
		if (startsWith(header, 0, ZIP_MAGIC)) {
			return byExtension(ZIP_BASED, extension).or(() -> Optional.of(ZIP));
		}
		if (startsWith(header, 0, OLE_MAGIC)) {
			return byExtension(OLE_BASED, extension);
		}
		if (startsWith(header, 4, FTYP)) {
			return Optional.of(MP4);
		}
		if (startsWith(header, 0, EBML)) {
			return Optional.of(WEBM);
		}
		if (startsWith(header, 0, OGG_MAGIC)) {
			return Optional.of(OGG);
		}
		if ("txt".equals(extension) && header.length > 0 && !containsNul(header)) {
			return Optional.of(TXT);
		}
		return Optional.empty();
	}

	private static Optional<AttachmentFormat> byExtension(Set<AttachmentFormat> candidates, String extension) {
		return candidates.stream().filter(f -> f.extension.equals(extension)).findFirst();
	}

	private static boolean containsNul(byte[] content) {
		for (byte b : content) {
			if (b == 0) {
				return true;
			}
		}
		return false;
	}

	private static boolean startsWith(byte[] content, int offset, byte[] magic) {
		return content.length >= offset + magic.length
			&& Arrays.equals(content, offset, offset + magic.length, magic, 0, magic.length);
	}
}
