package com.bohakka.renthub.event;

/** A chat attachment was stored (D-57); if the message's transaction rolls back, the file is removed again. */
public record ChatAttachmentUploaded(String key) {
}
