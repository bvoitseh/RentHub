package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A text message (D-57). */
public record SendMessageRequest(@NotBlank @Size(max = ChatMessage.BODY_MAX) String body) {
}
