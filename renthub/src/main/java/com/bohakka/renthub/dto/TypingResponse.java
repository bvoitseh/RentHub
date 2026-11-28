package com.bohakka.renthub.dto;

/** "The other side is typing" (D-58), pushed to {@code /user/queue/typing}. */
public record TypingResponse(Long conversationId, Long userId) {
}
