package com.bohakka.renthub.event;

/** A participant is typing in a conversation (D-58); forwarded to the other participant only. */
public record TypingSignal(Long conversationId, Long userId, String recipient) {
}
