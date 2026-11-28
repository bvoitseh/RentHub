import { NavLink } from 'react-router'
import { ChatAvatar } from '@/features/chat/components/ChatAvatar'
import { formatMessageTime } from '@/features/chat/format'
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
  `flex items-center gap-3 rounded-lg border border-l-4 p-3 text-sm transition-colors ${
    isActive
      ? 'border-brand-600 bg-brand-50 shadow-sm'
      : 'border-slate-200 border-l-transparent bg-surface hover:border-slate-300 hover:bg-slate-50'
  }`

/** One conversation: avatar, name, time of the last message, its preview and the unread counter. */
export function ConversationItem({ conversation: c, userId }: { conversation: Conversation; userId: number }) {
  const unread = c.unreadCount > 0
  return (
    <li>
      <NavLink to={`/messages/${c.id}`} className={itemClass}>
        <ChatAvatar id={c.other.id} name={c.other.name} />
        <span className="flex min-w-0 flex-1 flex-col gap-0.5">
          <span className="flex items-center justify-between gap-2">
            <span className={`truncate text-slate-900 ${unread ? 'font-bold' : 'font-semibold'}`}>{c.other.name}</span>
            {c.lastMessageAt && (
              <span className={`shrink-0 text-xs ${unread ? 'font-semibold text-brand-700' : 'text-slate-500'}`}>
                {formatMessageTime(c.lastMessageAt)}
              </span>
            )}
          </span>
          <span className="flex items-center justify-between gap-2">
            <span className={`truncate ${unread ? 'font-medium text-slate-800' : 'text-slate-500'}`}>
              {preview(c, userId)}
            </span>
            {unread && (
              <span
                className="shrink-0 rounded-full bg-brand-600 px-2 text-xs font-semibold text-on-accent"
                aria-label={t.unread(c.unreadCount)}
              >
                {c.unreadCount}
              </span>
            )}
          </span>
        </span>
      </NavLink>
    </li>
  )
}
