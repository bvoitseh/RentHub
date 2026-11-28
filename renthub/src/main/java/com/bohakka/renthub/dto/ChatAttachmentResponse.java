package com.bohakka.renthub.dto;

/** An attachment's metadata; {@code url} is the backend path that streams it to the participants only. */
public record ChatAttachmentResponse(String name, String contentType, long size, String url) {
}
