package com.bohakka.renthub.dto;

/** Unread messages across all of the user's conversations, for the menu badge (D-57). */
public record ChatUnreadResponse(long total) {
}
