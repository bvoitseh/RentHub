package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ChatAttachmentResponse;
import com.bohakka.renthub.dto.ChatMessageResponse;
import com.bohakka.renthub.dto.ChatPartyResponse;
import com.bohakka.renthub.dto.ConversationResponse;
import com.bohakka.renthub.entity.ChatMessage;
import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Chat entities → DTOs (D-57); attachments are referenced by the backend path that checks access. */
@Component
@RequiredArgsConstructor
class ChatMapper {

	static final String ATTACHMENT_PATH = "/api/v1/chats/attachments/";

	private final PresenceService presence;

	ChatMessageResponse message(ChatMessage m) {
		ChatAttachmentResponse attachment = m.hasAttachment()
			? new ChatAttachmentResponse(m.getAttachmentName(), m.getAttachmentType(), m.getAttachmentSize(),
				ATTACHMENT_PATH + m.getId())
			: null;
		return new ChatMessageResponse(m.getId(), m.getConversation().getId(), m.getSender().getId(), m.getKind(),
			m.getBody(), attachment, m.getCreatedAt());
	}

	/** D-58: the status is shown only when both the user and the viewer show their own. */
	ChatPartyResponse party(User user, User viewer) {
		boolean visible = user.isShowOnlineStatus() && viewer.isShowOnlineStatus();
		return new ChatPartyResponse(user.getId(), user.getName(), ChatAccess.available(user),
			visible ? presence.isOnline(user.getId()) : null, visible ? presence.lastSeen(user.getId()) : null);
	}

	ConversationResponse conversation(Conversation c, Long viewerId, ChatMessage last, long unread) {
		User viewer = c.getUserLow().getId().equals(viewerId) ? c.getUserLow() : c.getUserHigh();
		return new ConversationResponse(c.getId(), party(c.other(viewerId), viewer), last == null ? null : message(last),
			unread, c.getLastMessageAt());
	}
}
