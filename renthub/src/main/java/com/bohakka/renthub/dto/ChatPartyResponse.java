package com.bohakka.renthub.dto;

import java.time.Instant;

/**
 * The other side of a chat (D-57); {@code available} is false when they are blocked or deactivated. {@code online}
 * and {@code lastSeenAt} (D-58) are null when either side hides their online status.
 */
public record ChatPartyResponse(Long id, String name, boolean available, Boolean online, Instant lastSeenAt) {
}
