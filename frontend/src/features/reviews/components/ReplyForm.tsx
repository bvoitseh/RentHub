import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { useReply } from '@/features/reviews/hooks/useReviews'
import { uk } from '@/i18n/uk'

const t = uk.reviews.reply

/** FR-18: the target's single public reply; opened on demand, not editable afterwards. */
export function ReplyForm({ reviewId }: { reviewId: number }) {
  const [open, setOpen] = useState(false)
  const [text, setText] = useState('')
  const reply = useReply(reviewId)
  const value = text.trim()

  if (!open) {
    return (
      <Button variant="ghost" className="self-start px-0 text-brand-700" onClick={() => setOpen(true)}>
        {t.write}
      </Button>
    )
  }

  return (
    <div className="flex flex-col gap-2">
      <textarea
        aria-label={t.label}
        placeholder={t.placeholder}
        rows={2}
        maxLength={1000}
        value={text}
        onChange={(e) => setText(e.target.value)}
        className="block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-2 focus:outline-brand-600"
      />
      <p className="text-xs text-slate-500">{t.hint}</p>
      {reply.isError && <Alert tone="error">{problemMessage(reply.error)}</Alert>}
      <Button
        className="self-start"
        disabled={value.length === 0}
        loading={reply.isPending}
        onClick={() => reply.mutate(value)}
      >
        {t.submit}
      </Button>
    </div>
  )
}
