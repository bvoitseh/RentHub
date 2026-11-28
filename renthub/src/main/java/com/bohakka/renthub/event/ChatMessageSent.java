package com.bohakka.renthub.event;

import com.bohakka.renthub.dto.ChatMessageResponse;
import java.util.List;

/**
 * A chat message was saved (D-57); after the commit it is pushed over WebSocket to both participants, identified by
 * their login names (the e-mail addresses that are the STOMP user names).
 */
public record ChatMessageSent(ChatMessageResponse message, List<String> recipients) {
}
