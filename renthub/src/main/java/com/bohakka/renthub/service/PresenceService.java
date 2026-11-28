package com.bohakka.renthub.service;

import java.time.Instant;

/**
 * Who is online in the chat (D-58): a user is online while at least one of their WebSocket sessions is open; when the
 * last one closes, the time becomes their last-seen time. Kept in memory only, so a restart forgets it.
 */
public interface PresenceService {

	void connected(Long userId, String sessionId);

	void disconnected(Long userId, String sessionId);

	boolean isOnline(Long userId);

	/** When the user's last session closed, or null if unknown (never seen since the start, or online now). */
	Instant lastSeen(Long userId);
}
