import { api } from '@/api/client'
import { uploadWithProgress } from '@/api/uploadWithProgress'
import type { AttachmentKind, ChatMessage, Conversation } from '@/features/chat/types'

export const MESSAGES_PAGE = 30

/** Chat REST API (D-57); new messages also arrive over the WebSocket (useChatSocket). */
export const chatApi = {
  list: (signal?: AbortSignal) => api.get<Conversation[]>('chats', undefined, signal),
  get: (id: number, signal?: AbortSignal) => api.get<Conversation>(`chats/${id}`, undefined, signal),
  open: (userId: number) => api.post<Conversation>('chats', { userId }),
  unread: (signal?: AbortSignal) => api.get<{ total: number }>('chats/unread', undefined, signal),
  history: (id: number, before: number | undefined, signal?: AbortSignal) =>
    api.get<ChatMessage[]>(`chats/${id}/messages`, { before, size: MESSAGES_PAGE }, signal),
  sendText: (id: number, body: string) => api.post<ChatMessage>(`chats/${id}/messages`, { body }),
  sendAttachment: (id: number, kind: AttachmentKind, file: File, onProgress: (percent: number) => void) =>
    uploadWithProgress<ChatMessage>(`chats/${id}/attachments`, file, onProgress, { kind }),
  markRead: (id: number) => api.post<void>(`chats/${id}/read`),
}
