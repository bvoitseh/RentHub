import { NavLink } from 'react-router'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { formatMessageTime } from '@/features/chat/format'
import { useConversations } from '@/features/chat/hooks/useChatQueries'
import type { Conversation } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

const t = uk.chat

function preview(c: Conversation, userId: number) {
  const m = c.lastMessage
  if (!m) return ''
  const text = m.kind === 'TEXT' ? (m.body ?? '') : m.kind === 'FILE' ? (m.attachment?.name ?? t.kinds.FILE) : t.kinds[m.kind]
  return m.senderId === userId ? `${t.you}: ${text}` : text
}

const itemClass = ({ isActive }: { isActive: boolean }) =>
  `flex flex-col gap-0.5 rounded-md px-3 py-2 text-sm ${isActive ? 'bg-brand-50' : 'hover:bg-slate-100'}`

/** The user's conversations, newest first, with the last message and the unread count. */
export function ConversationList({ userId }: { userId: number }) {
  const { data, isPending, isError, error, refetch } = useConversations()

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  if (data.length === 0) return <EmptyState title={t.empty} description={t.emptyHint} />

  return (
    <ul className="flex flex-col gap-1">
      {data.map((c) => (
        <li key={c.id}>
          <NavLink to={`/messages/${c.id}`} className={itemClass}>
            <span className="flex items-center justify-between gap-2">
              <span className="truncate font-semibold text-slate-900">{c.other.name}</span>
              {c.lastMessageAt && (
                <span className="shrink-0 text-xs text-slate-500">{formatMessageTime(c.lastMessageAt)}</span>
              )}
            </span>
            <span className="flex items-center justify-between gap-2">
              <span className="truncate text-slate-600">{preview(c, userId)}</span>
              {c.unreadCount > 0 && (
                <span
                  className="shrink-0 rounded-full bg-brand-600 px-2 text-xs font-semibold text-on-accent"
                  aria-label={t.unread(c.unreadCount)}
                >
                  {c.unreadCount}
                </span>
              )}
            </span>
          </NavLink>
        </li>
      ))}
    </ul>
  )
}
