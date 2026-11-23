import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { FormField } from '@/components/ui/FormField'
import { uk } from '@/i18n/uk'

const t = uk.booking.actions

export interface CancelDialogProps {
  open: boolean
  /** FR-13a: the owner must give a reason of 10–500 characters; the renter needs none. */
  needsReason: boolean
  loading: boolean
  error: unknown
  onConfirm: (reason?: string) => void
  onClose: () => void
}

export function CancelDialog({ open, needsReason, loading, error, onConfirm, onClose }: CancelDialogProps) {
  const [reason, setReason] = useState('')
  const valid = !needsReason || (reason.trim().length >= 10 && reason.trim().length <= 500)

  return (
    <Dialog open={open} title={t.cancelTitle} onClose={onClose}>
      <div className="flex flex-col gap-3">
        <p className="text-sm text-slate-600">{needsReason ? t.cancelOwnerText : t.cancelRenterText}</p>
        {needsReason && (
          <FormField label={t.cancelReason} htmlFor="cancel-reason" hint={t.cancelReasonHint}>
            <textarea
              id="cancel-reason"
              rows={3}
              maxLength={500}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-2 focus:outline-brand-600"
            />
          </FormField>
        )}
        {Boolean(error) && <Alert tone="error">{problemMessage(error)}</Alert>}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            {t.back}
          </Button>
          <Button
            variant="danger"
            loading={loading}
            disabled={!valid}
            onClick={() => onConfirm(needsReason ? reason.trim() : undefined)}
          >
            {t.cancel}
          </Button>
        </div>
      </div>
    </Dialog>
  )
}
