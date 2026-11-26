package com.bohakka.renthub.dto;

/** FR-20: the outcome of a block or unblock and how many future bookings were cancelled by it. */
public record ModerationResult(Long id, boolean blocked, int cancelledBookings) {
}
