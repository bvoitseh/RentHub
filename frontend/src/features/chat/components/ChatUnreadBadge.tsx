import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useUnreadTotal } from '@/features/chat/hooks/useChatQueries'
import { uk } from '@/i18n/uk'

/** Unread messages across all conversations, shown next to «Повідомлення» in the side menu (D-57). */
export function ChatUnreadBadge() {
  const { data: user } = useCurrentUser()
  const { data: total } = useUnreadTotal(user?.role === 'USER')
  if (!total) return null
  return (
    <span
      className="ml-auto rounded-full bg-brand-600 px-2 text-xs font-semibold text-on-accent"
      aria-label={uk.chat.unread(total)}
    >
      {total}
    </span>
  )
}
