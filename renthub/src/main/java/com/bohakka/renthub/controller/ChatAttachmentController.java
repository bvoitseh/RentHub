package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.ChatAttachmentFile;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.ChatMessageService;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/v1/chats/attachments/{messageId} — streams a chat attachment to a participant (D-57). A {@code Range}
 * request gets 206 with that byte range (the first one if several), so audio and video can be seeked; images, PDF
 * and media are shown inline, other files are downloads. Cached privately by the browser only.
 */
@RestController
@RequiredArgsConstructor
class ChatAttachmentController {

	private final ChatMessageService messages;

	@GetMapping("/api/v1/chats/attachments/{messageId:\\d+}")
	ResponseEntity<Resource> download(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long messageId,
			@RequestHeader HttpHeaders headers) {
		ChatAttachmentFile file = messages.attachment(principal.getId(), messageId);
		long size = file.size();
		long start = 0;
		long end = size - 1;
		HttpStatus status = HttpStatus.OK;
		List<HttpRange> ranges = ranges(headers);
		if (!ranges.isEmpty()) {
			try {
				start = ranges.getFirst().getRangeStart(size);
				end = ranges.getFirst().getRangeEnd(size);
				status = HttpStatus.PARTIAL_CONTENT;
			} catch (IllegalArgumentException ex) {
				return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
					.header(HttpHeaders.CONTENT_RANGE, "bytes */" + size).build();
			}
		}
		ContentDisposition disposition = (file.inline() ? ContentDisposition.inline() : ContentDisposition.attachment())
			.filename(file.name(), StandardCharsets.UTF_8).build();
		ResponseEntity.BodyBuilder response = ResponseEntity.status(status)
			.contentType(MediaType.parseMediaType(file.contentType()))
			.contentLength(end - start + 1)
			.header(HttpHeaders.ACCEPT_RANGES, "bytes")
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
			.cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate());
		if (status == HttpStatus.PARTIAL_CONTENT) {
			response.header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + size);
		}
		return response.body(new InputStreamResource(messages.read(file, start, end)));
	}

	private static List<HttpRange> ranges(HttpHeaders headers) {
		try {
			return headers.getRange();
		} catch (IllegalArgumentException ex) {
			return List.of();
		}
	}
}
