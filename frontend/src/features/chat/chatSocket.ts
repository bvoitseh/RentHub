import type { Client } from '@stomp/stompjs'

/** Typing signals are sent at most this often per conversation (D-58). */
const TYPING_EVERY_MS = 2000

let client: Client | null = null
const lastSent = new Map<number, number>()

/** The open STOMP client, set by useChatSocket (null while disconnected). */
export function setChatClient(next: Client | null) {
  client = next
}

/** Tells the other participant that the user is typing; throttled, and silently skipped while offline. */
export function sendTyping(conversationId: number) {
  const now = Date.now()
  if (!client?.connected || now - (lastSent.get(conversationId) ?? 0) < TYPING_EVERY_MS) return
  lastSent.set(conversationId, now)
  client.publish({ destination: `/app/chat/${conversationId}/typing` })
}
