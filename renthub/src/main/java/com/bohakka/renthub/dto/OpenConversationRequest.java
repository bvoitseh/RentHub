package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotNull;

/** Opens (finds or creates) the conversation with this user (D-57). */
public record OpenConversationRequest(@NotNull Long userId) {
}
