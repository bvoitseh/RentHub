package com.bohakka.renthub.dto;

/** FR-3b: the other party of a CONFIRMED or COMPLETED booking, so the two can arrange the handover. */
public record ContactResponse(Long userId, String name, String phone) {
}
