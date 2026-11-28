/** Chat (D-57): conversations between two users and their messages. */
export type MessageKind = 'TEXT' | 'FILE' | 'VOICE' | 'VIDEO'
export type AttachmentKind = Exclude<MessageKind, 'TEXT'>

export interface ChatAttachment {
  name: string
  contentType: string
  size: number
  /** Same-origin backend path; it streams the file to the participants only. */
  url: string
}

export interface ChatMessage {
  id: number
  conversationId: number
  senderId: number
  kind: MessageKind
  body: string | null
  attachment: ChatAttachment | null
  createdAt: string
}

export interface ChatParty {
  id: number
  name: string
  /** False when the user is blocked or deactivated: they cannot receive messages. */
  available: boolean
}

export interface Conversation {
  id: number
  other: ChatParty
  lastMessage: ChatMessage | null
  unreadCount: number
  lastMessageAt: string | null
}

/** Shown above the thread when the chat was opened from a booking (router state, not stored). */
export interface BookingContext {
  title: string
  period: string
}
