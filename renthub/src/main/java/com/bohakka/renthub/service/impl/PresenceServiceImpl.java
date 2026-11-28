package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.event.PresenceChanged;
import com.bohakka.renthub.service.PresenceService;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * {@link PresenceService} in memory (D-58), fitting the single-instance deployment: open session ids per user and the
 * last-seen time per user. Going online (first session) or offline (last session closed) publishes
 * {@link PresenceChanged}; extra tabs of an online user change nothing.
 */
@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

	private final Map<Long, Set<String>> sessions = new ConcurrentHashMap<>();
	private final Map<Long, Instant> lastSeen = new ConcurrentHashMap<>();
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
	public void connected(Long userId, String sessionId) {
		boolean[] first = {false};
		sessions.compute(userId, (id, open) -> {
			Set<String> set = open == null ? ConcurrentHashMap.newKeySet() : open;
			first[0] = set.isEmpty();
			set.add(sessionId);
			return set;
		});
		if (first[0]) {
			events.publishEvent(new PresenceChanged(userId));
		}
	}

	@Override
	public void disconnected(Long userId, String sessionId) {
		boolean[] last = {false};
		sessions.computeIfPresent(userId, (id, open) -> {
			open.remove(sessionId);
			if (open.isEmpty()) {
				last[0] = true;
				lastSeen.put(userId, clock.instant());
				return null;
			}
			return open;
		});
		if (last[0]) {
			events.publishEvent(new PresenceChanged(userId));
		}
	}

	@Override
	public boolean isOnline(Long userId) {
		return sessions.containsKey(userId);
	}

	@Override
	public Instant lastSeen(Long userId) {
		return isOnline(userId) ? null : lastSeen.get(userId);
	}
}
