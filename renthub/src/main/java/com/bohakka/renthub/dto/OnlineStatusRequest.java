package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotNull;

/** D-58: show or hide when the user is online and when they were last seen in chats. */
public record OnlineStatusRequest(@NotNull Boolean visible) {
}
