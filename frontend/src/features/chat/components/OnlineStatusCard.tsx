import { useMutation, useQueryClient } from '@tanstack/react-query'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Checkbox } from '@/components/ui/Checkbox'
import { accountApi } from '@/features/auth/api/accountApi'
import { currentUserKey } from '@/features/auth/hooks/useCurrentUser'
import type { User } from '@/features/auth/types'
import { chatKeys } from '@/features/chat/hooks/useChatQueries'
import { uk } from '@/i18n/uk'

const t = uk.chat.status

/** D-58: «Показувати, коли я в мережі» — off hides the user's status and others' status from them. */
export function OnlineStatusCard({ user }: { user: User }) {
  const queryClient = useQueryClient()
  const change = useMutation({
    mutationFn: accountApi.setOnlineStatus,
    onSuccess: (updated) => {
      queryClient.setQueryData(currentUserKey, updated)
      void queryClient.invalidateQueries({ queryKey: chatKeys.all })
    },
  })

  return (
    <section className="rounded-lg border border-slate-200 bg-surface p-6">
      <h2 className="text-lg font-semibold text-slate-900">{t.title}</h2>
      <p className="mt-1 text-sm text-slate-600">{t.text}</p>
      <Checkbox
        id="online-status"
        className="mt-4"
        label={t.label}
        checked={user.showOnlineStatus}
        disabled={change.isPending}
        onChange={(event) => change.mutate(event.target.checked)}
      />
      {change.isError && (
        <Alert tone="error" className="mt-3">
          {problemMessage(change.error)}
        </Alert>
      )}
      {change.isSuccess && (
        <Alert tone="success" className="mt-3">
          {change.data.showOnlineStatus ? t.shown : t.hidden}
        </Alert>
      )}
    </section>
  )
}
