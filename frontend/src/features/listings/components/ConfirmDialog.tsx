import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { uk } from '@/i18n/uk'

export interface ConfirmDialogProps {
  open: boolean
  title: string
  text: string
  confirmLabel: string
  danger?: boolean
  loading?: boolean
  onConfirm: () => void
  onClose: () => void
}

/** Yes/cancel confirmation for archive and delete. */
export function ConfirmDialog({ open, title, text, confirmLabel, danger, loading, onConfirm, onClose }: ConfirmDialogProps) {
  return (
    <Dialog open={open} title={title} onClose={onClose}>
      <p className="mb-4 text-sm text-slate-600">{text}</p>
      <div className="flex justify-end gap-2">
        <Button variant="secondary" onClick={onClose}>
          {uk.listings.cancel}
        </Button>
        <Button variant={danger ? 'danger' : 'primary'} loading={loading} onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </div>
    </Dialog>
  )
}
