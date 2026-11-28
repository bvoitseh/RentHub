package com.bohakka.renthub.dto;

import java.time.Instant;

/**
 * A chat partner's status (D-58), pushed to {@code /user/queue/presence}: {@code online} and {@code lastSeenAt} are
 * null when either side hides their online status; {@code lastSeenAt} is null too when it is unknown (memory only).
 */
public record PresenceResponse(Long userId, Boolean online, Instant lastSeenAt) {
}
