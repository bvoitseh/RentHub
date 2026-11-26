import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { useModerate } from '@/features/admin/hooks/useAdmin'
import { ConfirmDialog } from '@/features/listings/components/ConfirmDialog'
import { uk } from '@/i18n/uk'

const t = uk.admin

export interface BlockToggleProps {
  kind: 'users' | 'equipment'
  id: number
  blocked: boolean
}

/** FR-20: block / unblock with a confirmation that says what happens to future bookings. */
export function BlockToggle({ kind, id, blocked }: BlockToggleProps) {
  const [open, setOpen] = useState(false)
  const moderate = useModerate(kind)
  const user = kind === 'users'
  const title = blocked
    ? user
      ? t.unblockUserTitle
      : t.unblockEquipmentTitle
    : user
      ? t.blockUserTitle
      : t.blockEquipmentTitle
  const text = blocked
    ? user
      ? t.unblockUserText
      : t.unblockEquipmentText
    : user
      ? t.blockUserText
      : t.blockEquipmentText

  return (
    <div className="flex flex-col items-end gap-1">
      <Button variant={blocked ? 'secondary' : 'danger'} onClick={() => setOpen(true)}>
        {blocked ? t.unblock : t.block}
      </Button>
      {moderate.data && moderate.data.cancelledBookings > 0 && (
        <span className="text-xs text-slate-500">{t.cancelled(moderate.data.cancelledBookings)}</span>
      )}
      {moderate.isError && <Alert tone="error">{problemMessage(moderate.error)}</Alert>}
      <ConfirmDialog
        open={open}
        title={title}
        text={text}
        confirmLabel={blocked ? t.unblock : t.block}
        cancelLabel={t.cancel}
        danger={!blocked}
        loading={moderate.isPending}
        onClose={() => setOpen(false)}
        onConfirm={() =>
          moderate.mutate({ id, action: blocked ? 'unblock' : 'block' }, { onSettled: () => setOpen(false) })
        }
      />
    </div>
  )
}
