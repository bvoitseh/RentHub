import { useEffect } from 'react'
import { Client } from '@stomp/stompjs'
import { useQueryClient, type QueryClient } from '@tanstack/react-query'
import { setChatClient } from '@/features/chat/chatSocket'
import { addMessageToCache, chatKeys } from '@/features/chat/hooks/useChatQueries'
import { clearTyping, markTyping } from '@/features/chat/typingStore'
import type { ChatMessage, Conversation, Presence } from '@/features/chat/types'

function socketUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws`
}

/** Puts a partner's new status (D-58) into the conversation list and any open conversation with them. */
function applyPresence(queryClient: QueryClient, p: Presence) {
  const update = (c: Conversation): Conversation =>
    c.other.id === p.userId ? { ...c, other: { ...c.other, online: p.online, lastSeenAt: p.lastSeenAt } } : c
  queryClient.setQueryData<Conversation[]>(chatKeys.conversations, (list) => list?.map(update))
  queryClient.setQueriesData<Conversation>({ queryKey: ['chat', 'conversation'] }, (c) => (c ? update(c) : c))
}

/**
 * Keeps a STOMP-over-WebSocket connection while a user is signed in (D-57, D-58): new messages go into the query
 * cache, partners' online status updates the conversations, typing signals feed «друкує…». The handshake carries the
 * session cookie, so no token is needed; it reconnects every 5 s after a drop.
 */
export function useChatSocket(enabled: boolean) {
  const queryClient = useQueryClient()

  useEffect(() => {
    if (!enabled) return
    const client = new Client({
      brokerURL: socketUrl(),
      reconnectDelay: 5000,
      onConnect: () => {
        setChatClient(client)
        client.subscribe('/user/queue/chat', (frame) => {
          const message = JSON.parse(frame.body) as ChatMessage
          clearTyping(message.conversationId)
          addMessageToCache(queryClient, message)
        })
        client.subscribe('/user/queue/presence', (frame) => applyPresence(queryClient, JSON.parse(frame.body) as Presence))
        client.subscribe('/user/queue/typing', (frame) => {
          markTyping((JSON.parse(frame.body) as { conversationId: number }).conversationId)
        })
      },
      onWebSocketClose: () => setChatClient(null),
    })
    client.activate()
    return () => {
      setChatClient(null)
      void client.deactivate()
    }
  }, [enabled, queryClient])
}
