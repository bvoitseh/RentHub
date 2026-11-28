package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.MessageKind;
import java.util.Set;
import org.springframework.util.unit.DataSize;

/**
 * Limits per attachment kind (D-57): documents and images up to 20 MB, voice messages up to 10 MB (about five minutes
 * of Opus audio; the recorder stops at five minutes), videos MP4/WebM up to 50 MB.
 */
enum AttachmentRules {
	FILE(AttachmentFormat.DOCUMENTS, 20, "file"),
	VOICE(AttachmentFormat.AUDIO, 10, "voice"),
	VIDEO(AttachmentFormat.VIDEOS, 50, "video");

	private final Set<AttachmentFormat> formats;
	private final int maxMegabytes;
	private final String defaultName;

	AttachmentRules(Set<AttachmentFormat> formats, int maxMegabytes, String defaultName) {
		this.formats = formats;
		this.maxMegabytes = maxMegabytes;
		this.defaultName = defaultName;
	}

	static AttachmentRules of(MessageKind kind) {
		return switch (kind) {
			case FILE -> FILE;
			case VOICE -> VOICE;
			case VIDEO -> VIDEO;
			case TEXT -> throw new IllegalArgumentException("TEXT has no attachment");
		};
	}

	boolean allows(AttachmentFormat format) {
		return formats.contains(format);
	}

	int maxMegabytes() {
		return maxMegabytes;
	}

	long maxBytes() {
		return DataSize.ofMegabytes(maxMegabytes).toBytes();
	}

	/** Voice recordings in WebM / MP4 containers are audio, not video. */
	String contentType(AttachmentFormat format) {
		if (this == VOICE && format == AttachmentFormat.WEBM) {
			return "audio/webm";
		}
		if (this == VOICE && format == AttachmentFormat.MP4) {
			return "audio/mp4";
		}
		return format.contentType();
	}

	/** The stored file name: the original one when it is usable, else e.g. "voice.webm". */
	String fileName(String original, AttachmentFormat format) {
		if (original == null || original.isBlank()) {
			return defaultName + "." + format.extension();
		}
		String base = original.replace('\\', '/');
		base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").strip();
		if (base.isEmpty()) {
			return defaultName + "." + format.extension();
		}
		return base.length() > 200 ? base.substring(base.length() - 200) : base;
	}
}
