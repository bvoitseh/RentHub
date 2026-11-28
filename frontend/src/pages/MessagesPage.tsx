import { useLocation, useParams } from 'react-router'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { ChatThread } from '@/features/chat/components/ChatThread'
import { ConversationList } from '@/features/chat/components/ConversationList'
import type { BookingContext } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

const t = uk.chat

/** /messages and /messages/:id (D-57): the conversation list beside the open thread; on phones one at a time. */
export function MessagesPage() {
  const { data: user } = useCurrentUser()
  const id = useParams().id
  const conversationId = id ? Number(id) : null
  const booking = (useLocation().state as { booking?: BookingContext } | null)?.booking
  if (!user) return null

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-bold text-slate-900">{t.title}</h1>
      <div className="grid gap-4 md:grid-cols-[18rem_1fr]">
        <aside className={conversationId ? 'hidden md:block' : ''}>
          <ConversationList userId={user.id} />
        </aside>
        {conversationId ? (
          <ChatThread
            key={conversationId}
            conversationId={conversationId}
            userId={user.id}
            emailVerified={user.emailVerified}
            booking={booking}
          />
        ) : (
          <p className="hidden self-center text-center text-sm text-slate-500 md:block">{t.choose}</p>
        )}
      </div>
    </div>
  )
}
