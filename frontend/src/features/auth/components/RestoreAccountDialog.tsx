import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { useRestoreAccount } from '@/features/auth/hooks/useAuthMutations'
import type { Credentials } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

export interface RestoreAccountDialogProps {
  credentials: Credentials | null
  onClose: () => void
  onRestored: () => void
}

/** FR-3c: offered when login answers `auth.deactivated`; restores with the same credentials. */
export function RestoreAccountDialog({ credentials, onClose, onRestored }: RestoreAccountDialogProps) {
  const restore = useRestoreAccount()
  const t = uk.auth.restore

  const confirm = async () => {
    if (!credentials) return
    await restore.mutateAsync(credentials)
    onRestored()
  }

  return (
    <Dialog open={credentials !== null} title={t.title} onClose={onClose}>
      <p className="text-sm text-slate-600">{t.text}</p>
      {restore.isError && (
        <Alert tone="error" className="mt-3">
          {problemMessage(restore.error)}
        </Alert>
      )}
      <div className="mt-5 flex justify-end gap-2">
        <Button variant="secondary" onClick={onClose} disabled={restore.isPending}>
          {t.cancel}
        </Button>
        <Button onClick={confirm} loading={restore.isPending}>
          {t.confirm}
        </Button>
      </div>
    </Dialog>
  )
}
