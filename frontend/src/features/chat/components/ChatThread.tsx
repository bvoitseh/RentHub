import { useCallback, useRef } from 'react'
import { Link } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { MessageComposer } from '@/features/chat/components/MessageComposer'
import { MessageList } from '@/features/chat/components/MessageList'
import { PresenceLine } from '@/features/chat/components/PresenceLine'
import { useMarkRead } from '@/features/chat/hooks/useChatMutations'
import { useConversation } from '@/features/chat/hooks/useChatQueries'
import type { BookingContext } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

const t = uk.chat

export interface ChatThreadProps {
  conversationId: number
  userId: number
  emailVerified: boolean
  booking?: BookingContext
}

/** One conversation: who it is with, the booking it was opened from, the messages and the composer. */
export function ChatThread({ conversationId, userId, emailVerified, booking }: ChatThreadProps) {
  const conversation = useConversation(conversationId)
  const markRead = useMarkRead(conversationId)
  const lastMarked = useRef(0)
  const { mutate } = markRead

  const onNewest = useCallback(
    (messageId: number) => {
      if (messageId <= lastMarked.current) return
      lastMarked.current = messageId
      mutate()
    },
    [mutate],
  )

  if (conversation.isPending) return <LoadingState />
  if (conversation.isError) return <ErrorState error={conversation.error} onRetry={() => void conversation.refetch()} />
  const other = conversation.data.other

  return (
    <section className="flex h-[calc(100dvh-11rem)] min-h-[26rem] flex-col rounded-lg border border-slate-200 bg-surface">
      <header className="flex flex-col gap-1 border-b border-slate-200 p-3">
        <Link to="/messages" className="text-sm text-brand-700 hover:underline md:hidden">
          {t.backToList}
        </Link>
        <Link to={`/users/${other.id}`} className="text-lg font-semibold text-slate-900 hover:text-brand-700">
          {other.name}
        </Link>
        <PresenceLine conversationId={conversationId} other={other} />
        {booking && <p className="text-sm text-slate-600">{t.aboutBooking(booking.title, booking.period)}</p>}
      </header>
      <MessageList conversationId={conversationId} userId={userId} onNewest={onNewest} />
      {!other.available ? (
        <Alert tone="warning" className="m-3">
          {t.unavailable}
        </Alert>
      ) : !emailVerified ? (
        <Alert tone="warning" className="m-3">
          {t.writeVerifyFirst}
        </Alert>
      ) : (
        <MessageComposer conversationId={conversationId} />
      )}
    </section>
  )
}
