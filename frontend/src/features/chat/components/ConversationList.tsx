import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { ConversationItem } from '@/features/chat/components/ConversationItem'
import { useConversations } from '@/features/chat/hooks/useChatQueries'
import { uk } from '@/i18n/uk'

const t = uk.chat

/** The user's conversations, newest first. */
export function ConversationList({ userId }: { userId: number }) {
  const { data, isPending, isError, error, refetch } = useConversations()

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  if (data.length === 0) return <EmptyState title={t.empty} description={t.emptyHint} />

  return (
    <ul className="flex flex-col gap-2">
      {data.map((c) => (
        <ConversationItem key={c.id} conversation={c} userId={userId} />
      ))}
    </ul>
  )
}
