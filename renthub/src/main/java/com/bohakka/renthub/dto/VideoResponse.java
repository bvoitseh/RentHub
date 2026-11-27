package com.bohakka.renthub.dto;

/** FR-4a (D-52): the listing's video, publicly readable by URL. */
public record VideoResponse(String url, String contentType) {
}
