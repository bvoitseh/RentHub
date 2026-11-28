package com.bohakka.renthub.event;

/** A user went online or offline, or changed whether they show it (D-58); their chat partners get the new status. */
public record PresenceChanged(Long userId) {
}
