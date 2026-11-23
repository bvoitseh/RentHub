import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { uk } from '@/i18n/uk'

export interface ConfirmDialogProps {
  open: boolean
  title: string
  text: string
  confirmLabel: string
  /** Dismiss button text; «Скасувати» by default. */
  cancelLabel?: string
  danger?: boolean
  loading?: boolean
  onConfirm: () => void
  onClose: () => void
}

/** Yes/cancel confirmation (archive, delete, reject a booking request). */
export function ConfirmDialog(props: ConfirmDialogProps) {
  const { open, title, text, confirmLabel, cancelLabel, danger, loading, onConfirm, onClose } = props
  return (
    <Dialog open={open} title={title} onClose={onClose}>
      <p className="mb-4 text-sm text-slate-600">{text}</p>
      <div className="flex justify-end gap-2">
        <Button variant="secondary" onClick={onClose}>
          {cancelLabel ?? uk.listings.cancel}
        </Button>
        <Button variant={danger ? 'danger' : 'primary'} loading={loading} onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </div>
    </Dialog>
  )
}
