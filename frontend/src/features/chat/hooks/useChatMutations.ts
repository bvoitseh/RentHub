import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router'
import { chatApi } from '@/features/chat/api/chatApi'
import { addMessageToCache, chatKeys } from '@/features/chat/hooks/useChatQueries'
import type { AttachmentKind, BookingContext } from '@/features/chat/types'

/** Opens (finds or creates) the conversation with a user and goes to it; a booking adds context to the thread. */
export function useOpenChat() {
  const navigate = useNavigate()
  return useMutation({
    mutationFn: ({ userId }: { userId: number; booking?: BookingContext }) => chatApi.open(userId),
    onSuccess: (conversation, { booking }) => navigate(`/messages/${conversation.id}`, { state: { booking } }),
  })
}

export function useSendText(conversationId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: string) => chatApi.sendText(conversationId, body),
    onSuccess: (message) => addMessageToCache(queryClient, message),
  })
}

/** Uploads a file, voice or video message with progress in percent (null when idle). */
export function useSendAttachment(conversationId: number) {
  const queryClient = useQueryClient()
  const [progress, setProgress] = useState<number | null>(null)
  const mutation = useMutation({
    mutationFn: ({ kind, file }: { kind: AttachmentKind; file: File }) => {
      setProgress(0)
      return chatApi.sendAttachment(conversationId, kind, file, setProgress)
    },
    onSuccess: (message) => addMessageToCache(queryClient, message),
    onSettled: () => setProgress(null),
  })
  return { ...mutation, progress }
}

/** Marks the conversation read and clears its unread counters in the cache. */
export function useMarkRead(conversationId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => chatApi.markRead(conversationId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: chatKeys.conversations })
      void queryClient.invalidateQueries({ queryKey: chatKeys.unread })
    },
  })
}
