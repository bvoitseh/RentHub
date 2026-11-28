package com.bohakka.renthub.dto;

/** The other side of a chat (D-57); {@code available} is false when they are blocked or deactivated. */
public record ChatPartyResponse(Long id, String name, boolean available) {
}
