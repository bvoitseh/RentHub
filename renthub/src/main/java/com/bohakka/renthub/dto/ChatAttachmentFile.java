package com.bohakka.renthub.dto;

/** What the controller needs to stream an attachment after the access check (D-57); never sent as JSON. */
public record ChatAttachmentFile(String key, String name, String contentType, long size, boolean inline) {
}
