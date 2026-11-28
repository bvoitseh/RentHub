package com.bohakka.renthub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One chat message (D-57): text, or an attachment (file, voice, video) kept in the object storage under a private key
 * and served only to the conversation's participants. Messages are never edited or deleted.
 */
@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	public static final int BODY_MAX = 4000;
	public static final int NAME_MAX = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "conversation_id", nullable = false, updatable = false)
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id", nullable = false, updatable = false)
	private User sender;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10, updatable = false)
	private MessageKind kind;

	@Column(length = BODY_MAX, updatable = false)
	private String body;

	@Column(name = "attachment_key", updatable = false)
	private String attachmentKey;

	@Column(name = "attachment_name", length = NAME_MAX, updatable = false)
	private String attachmentName;

	@Column(name = "attachment_type", length = 100, updatable = false)
	private String attachmentType;

	@Column(name = "attachment_size", updatable = false)
	private Long attachmentSize;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public static ChatMessage text(Conversation conversation, User sender, String body, Instant now) {
		ChatMessage m = create(conversation, sender, MessageKind.TEXT, now);
		m.body = body;
		return m;
	}

	public static ChatMessage attachment(Conversation conversation, User sender, MessageKind kind, String key,
			String name, String contentType, long size, Instant now) {
		if (kind == MessageKind.TEXT) {
			throw new IllegalArgumentException("An attachment message cannot be TEXT");
		}
		ChatMessage m = create(conversation, sender, kind, now);
		m.attachmentKey = key;
		m.attachmentName = name;
		m.attachmentType = contentType;
		m.attachmentSize = size;
		return m;
	}

	public boolean hasAttachment() {
		return kind != MessageKind.TEXT;
	}

	private static ChatMessage create(Conversation conversation, User sender, MessageKind kind, Instant now) {
		ChatMessage m = new ChatMessage();
		m.conversation = conversation;
		m.sender = sender;
		m.kind = kind;
		m.createdAt = now;
		conversation.messageSent(now);
		return m;
	}
}
