import { problemMessage } from '@/api/problem'
import { Button } from '@/components/ui/Button'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useOpenChat } from '@/features/chat/hooks/useChatMutations'
import type { BookingContext } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

export interface ChatButtonProps {
  userId: number
  /** When opened from a booking, its title and period are shown above the thread. */
  booking?: BookingContext
  variant?: 'primary' | 'secondary' | 'ghost'
}

/** «Написати»: opens the conversation with the user (D-57); hidden for guests, the admin and on one's own profile. */
export function ChatButton({ userId, booking, variant = 'secondary' }: ChatButtonProps) {
  const { data: user } = useCurrentUser()
  const open = useOpenChat()

  if (!user || user.role !== 'USER' || user.id === userId) return null

  return (
    <div className="flex flex-col items-start gap-1">
      <Button variant={variant} loading={open.isPending} onClick={() => open.mutate({ userId, booking })}>
        {uk.chat.write}
      </Button>
      {open.isError && <p className="text-xs text-red-700">{problemMessage(open.error)}</p>}
    </div>
  )
}
