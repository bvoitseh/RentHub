package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.ChatMessageResponse;
import com.bohakka.renthub.dto.ChatUnreadResponse;
import com.bohakka.renthub.dto.ConversationResponse;
import com.bohakka.renthub.dto.OpenConversationRequest;
import com.bohakka.renthub.dto.SendMessageRequest;
import com.bohakka.renthub.entity.MessageKind;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.ChatMessageService;
import com.bohakka.renthub.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * /api/v1/chats — conversations and messages between two users (D-57). Authenticated; the services check that the
 * caller takes part in the conversation. New messages are also pushed over WebSocket ({@code /ws}).
 */
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
class ChatController {

	private final ChatService chats;
	private final ChatMessageService messages;

	@GetMapping
	List<ConversationResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
		return chats.list(principal.getId());
	}

	@PostMapping
	ConversationResponse open(@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody OpenConversationRequest request) {
		return chats.open(principal.getId(), request.userId());
	}

	@GetMapping("/unread")
	ChatUnreadResponse unread(@AuthenticationPrincipal AppUserDetails principal) {
		return new ChatUnreadResponse(chats.unreadTotal(principal.getId()));
	}

	@GetMapping("/{id:\\d+}")
	ConversationResponse get(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return chats.get(principal.getId(), id);
	}

	@GetMapping("/{id:\\d+}/messages")
	List<ChatMessageResponse> history(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@RequestParam(required = false) Long before, @RequestParam(defaultValue = "30") int size) {
		return messages.history(principal.getId(), id, before, size);
	}

	@PostMapping("/{id:\\d+}/messages")
	@ResponseStatus(HttpStatus.CREATED)
	ChatMessageResponse sendText(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@Valid @RequestBody SendMessageRequest request) {
		return messages.sendText(principal.getId(), id, request.body());
	}

	/** Multipart: {@code file} and {@code kind} = FILE, VOICE or VIDEO. */
	@PostMapping(path = "/{id:\\d+}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	ChatMessageResponse sendAttachment(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id,
			@RequestParam MessageKind kind, @RequestPart("file") MultipartFile file) {
		return messages.sendAttachment(principal.getId(), id, kind, file);
	}

	@PostMapping("/{id:\\d+}/read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void markRead(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		chats.markRead(principal.getId(), id);
	}
}
