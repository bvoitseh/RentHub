import { formatLastSeen } from '@/features/chat/format'
import type { ChatParty } from '@/features/chat/types'
import { useTyping } from '@/features/chat/typingStore'
import { uk } from '@/i18n/uk'

const t = uk.chat.presence

/** Under the partner's name: «друкує…», «у мережі», «був(-ла) у мережі …» or «не в мережі» (D-58). */
export function PresenceLine({ conversationId, other }: { conversationId: number; other: ChatParty }) {
  const typing = useTyping(conversationId)
  if (typing) return <p className="text-sm font-medium text-brand-700">{t.typing}</p>
  if (other.online === null) return null
  if (other.online) return <p className="text-sm text-emerald-700">{t.online}</p>
  return (
    <p className="text-sm text-slate-500">
      {other.lastSeenAt ? t.lastSeen(formatLastSeen(other.lastSeenAt, t)) : t.offline}
    </p>
  )
}
