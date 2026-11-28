import { useEffect, useRef } from 'react'
import { Button } from '@/components/ui/Button'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { MessageBubble } from '@/features/chat/components/MessageBubble'
import { flattenMessages, useMessages } from '@/features/chat/hooks/useChatQueries'
import { uk } from '@/i18n/uk'

const t = uk.chat

export interface MessageListProps {
  conversationId: number
  userId: number
  /** Called with the newest loaded message id whenever it changes (the thread marks itself read). */
  onNewest: (messageId: number) => void
}

/** The thread's messages, oldest at the top; it scrolls to the newest one when a message arrives. */
export function MessageList({ conversationId, userId, onNewest }: MessageListProps) {
  const query = useMessages(conversationId)
  const messages = flattenMessages(query.data)
  const newestId = messages.at(-1)?.id
  const bottom = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (newestId === undefined) return
    bottom.current?.scrollIntoView({ block: 'end' })
    onNewest(newestId)
  }, [newestId, onNewest])

  if (query.isPending) return <LoadingState />
  if (query.isError) return <ErrorState error={query.error} onRetry={() => void query.refetch()} />

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto p-3">
      {query.hasNextPage && (
        <Button
          variant="ghost"
          className="self-center"
          loading={query.isFetchingNextPage}
          onClick={() => void query.fetchNextPage()}
        >
          {t.older}
        </Button>
      )}
      {messages.length === 0 ? (
        <p className="m-auto text-sm text-slate-500">{t.noMessages}</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {messages.map((m) => (
            <MessageBubble key={m.id} message={m} own={m.senderId === userId} />
          ))}
        </ul>
      )}
      <div ref={bottom} />
    </div>
  )
}
