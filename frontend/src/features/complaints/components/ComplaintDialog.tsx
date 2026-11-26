import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { FormField } from '@/components/ui/FormField'
import { uk } from '@/i18n/uk'

const t = uk.complaints

export interface ComplaintDialogProps {
  open: boolean
  title: string
  /** Shown instead of allowing to send, e.g. when the e-mail is not verified. */
  notice?: string
  loading: boolean
  error: unknown
  onSend: (description: string) => void
  onClose: () => void
}

/** FR-21: the description of 10–1000 characters; the server re-checks it. */
export function ComplaintDialog({ open, title, notice, loading, error, onSend, onClose }: ComplaintDialogProps) {
  const [text, setText] = useState('')
  const length = text.trim().length
  const valid = !notice && length >= 10 && length <= 1000

  return (
    <Dialog open={open} title={title} onClose={onClose}>
      <div className="flex flex-col gap-3">
        <p className="text-sm text-slate-600">{t.intro}</p>
        <FormField label={t.description} htmlFor="complaint-text" hint={t.descriptionHint}>
          <textarea
            id="complaint-text"
            rows={4}
            maxLength={1000}
            value={text}
            onChange={(e) => setText(e.target.value)}
            className="block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-2 focus:outline-brand-600"
          />
        </FormField>
        {notice && <Alert tone="warning">{notice}</Alert>}
        {Boolean(error) && <Alert tone="error">{problemMessage(error)}</Alert>}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            {t.back}
          </Button>
          <Button variant="danger" loading={loading} disabled={!valid} onClick={() => onSend(text.trim())}>
            {t.send}
          </Button>
        </div>
      </div>
    </Dialog>
  )
}
