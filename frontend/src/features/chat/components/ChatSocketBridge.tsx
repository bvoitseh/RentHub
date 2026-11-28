import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useChatSocket } from '@/features/chat/hooks/useChatSocket'

/** Keeps the chat WebSocket open while a regular user is signed in (D-57); renders nothing. */
export function ChatSocketBridge() {
  const { data: user } = useCurrentUser()
  useChatSocket(user?.role === 'USER')
  return null
}
