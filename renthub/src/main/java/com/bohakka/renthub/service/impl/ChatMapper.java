package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ChatAttachmentResponse;
import com.bohakka.renthub.dto.ChatMessageResponse;
import com.bohakka.renthub.dto.ChatPartyResponse;
import com.bohakka.renthub.dto.ConversationResponse;
import com.bohakka.renthub.entity.ChatMessage;
import com.bohakka.renthub.entity.Conversation;
import com.bohakka.renthub.entity.User;
import org.springframework.stereotype.Component;

/** Chat entities → DTOs (D-57); attachments are referenced by the backend path that checks access. */
@Component
class ChatMapper {

	static final String ATTACHMENT_PATH = "/api/v1/chats/attachments/";

	ChatMessageResponse message(ChatMessage m) {
		ChatAttachmentResponse attachment = m.hasAttachment()
			? new ChatAttachmentResponse(m.getAttachmentName(), m.getAttachmentType(), m.getAttachmentSize(),
				ATTACHMENT_PATH + m.getId())
			: null;
		return new ChatMessageResponse(m.getId(), m.getConversation().getId(), m.getSender().getId(), m.getKind(),
			m.getBody(), attachment, m.getCreatedAt());
	}

	ChatPartyResponse party(User user) {
		return new ChatPartyResponse(user.getId(), user.getName(), ChatAccess.available(user));
	}

	ConversationResponse conversation(Conversation c, Long viewerId, ChatMessage last, long unread) {
		return new ConversationResponse(c.getId(), party(c.other(viewerId)), last == null ? null : message(last), unread,
			c.getLastMessageAt());
	}
}
