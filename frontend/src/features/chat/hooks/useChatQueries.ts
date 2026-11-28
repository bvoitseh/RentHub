import { useInfiniteQuery, useQuery, type InfiniteData, type QueryClient } from '@tanstack/react-query'
import { chatApi, MESSAGES_PAGE } from '@/features/chat/api/chatApi'
import type { ChatMessage } from '@/features/chat/types'

export const chatKeys = {
  all: ['chat'] as const,
  conversations: ['chat', 'conversations'] as const,
  conversation: (id: number) => ['chat', 'conversation', id] as const,
  messages: (id: number) => ['chat', 'messages', id] as const,
  unread: ['chat', 'unread'] as const,
}

type MessagePages = InfiniteData<ChatMessage[], number | undefined>

export function useConversations() {
  return useQuery({ queryKey: chatKeys.conversations, queryFn: ({ signal }) => chatApi.list(signal) })
}

export function useConversation(id: number) {
  return useQuery({ queryKey: chatKeys.conversation(id), queryFn: ({ signal }) => chatApi.get(id, signal) })
}

export function useUnreadTotal(enabled: boolean) {
  return useQuery({
    queryKey: chatKeys.unread,
    queryFn: ({ signal }) => chatApi.unread(signal),
    enabled,
    select: (data) => data.total,
  })
}

/** Pages of history, newest page first; each page is in chronological order. */
export function useMessages(id: number) {
  return useInfiniteQuery({
    queryKey: chatKeys.messages(id),
    queryFn: ({ pageParam, signal }) => chatApi.history(id, pageParam, signal),
    initialPageParam: undefined as number | undefined,
    getNextPageParam: (lastPage) => (lastPage.length === MESSAGES_PAGE ? lastPage[0]?.id : undefined),
  })
}

/** The whole loaded thread in chronological order. */
export function flattenMessages(data: InfiniteData<ChatMessage[], unknown> | undefined): ChatMessage[] {
  return data ? [...data.pages].reverse().flat() : []
}

/**
 * Adds a message to its loaded thread (ignoring duplicates: the sender gets it from the POST answer and the socket)
 * and refreshes the conversation list and unread counter.
 */
export function addMessageToCache(queryClient: QueryClient, message: ChatMessage) {
  queryClient.setQueryData<MessagePages>(chatKeys.messages(message.conversationId), (data) => {
    if (!data || data.pages.some((page) => page.some((m) => m.id === message.id))) return data
    const [newest = [], ...older] = data.pages
    return { ...data, pages: [[...newest, message], ...older] }
  })
  void queryClient.invalidateQueries({ queryKey: chatKeys.conversations })
  void queryClient.invalidateQueries({ queryKey: chatKeys.conversation(message.conversationId) })
  void queryClient.invalidateQueries({ queryKey: chatKeys.unread })
}
