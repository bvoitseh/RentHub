import { useEffect } from 'react'
import { Client } from '@stomp/stompjs'
import { useQueryClient } from '@tanstack/react-query'
import { addMessageToCache } from '@/features/chat/hooks/useChatQueries'
import type { ChatMessage } from '@/features/chat/types'

/** Where the backend pushes this user's new messages (D-57). */
const DESTINATION = '/user/queue/chat'

function socketUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws`
}

/**
 * Keeps a STOMP-over-WebSocket connection while a user is signed in and puts every pushed message into the query
 * cache. The handshake carries the session cookie, so no token is needed; it reconnects every 5 s after a drop.
 */
export function useChatSocket(enabled: boolean) {
  const queryClient = useQueryClient()

  useEffect(() => {
    if (!enabled) return
    const client = new Client({
      brokerURL: socketUrl(),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(DESTINATION, (frame) => {
          addMessageToCache(queryClient, JSON.parse(frame.body) as ChatMessage)
        })
      },
    })
    client.activate()
    return () => {
      void client.deactivate()
    }
  }, [enabled, queryClient])
}
