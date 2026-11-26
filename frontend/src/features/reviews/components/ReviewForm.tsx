import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { formatDateTime } from '@/features/booking/dates'
import { StarInput } from '@/features/reviews/components/StarInput'
import { useCreateReview } from '@/features/reviews/hooks/useReviews'
import { uk } from '@/i18n/uk'

const t = uk.reviews

/** FR-18: rating and comment for the other side of a completed booking, until the deadline. */
export function ReviewForm({ bookingId, until }: { bookingId: number; until: string | null }) {
  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const create = useCreateReview(bookingId)
  const text = comment.trim()
  const valid = rating >= 1 && text.length > 0 && text.length <= 1000

  return (
    <div className="flex flex-col gap-2 rounded-md border border-slate-200 p-3">
      <p className="text-sm font-semibold text-slate-900">{t.form.title}</p>
      {until && <p className="text-xs text-slate-500">{t.form.until(formatDateTime(until))}</p>}
      <StarInput name={`review-rating-${bookingId}`} value={rating} onChange={setRating} />
      <FormField label={t.form.comment} htmlFor={`review-${bookingId}`} hint={t.form.commentHint}>
        <textarea
          id={`review-${bookingId}`}
          rows={3}
          maxLength={1000}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          className="block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-2 focus:outline-brand-600"
        />
      </FormField>
      <p className="text-xs text-slate-500">{t.form.blindNote}</p>
      {create.isError && <Alert tone="error">{problemMessage(create.error)}</Alert>}
      <Button
        className="self-start"
        disabled={!valid}
        loading={create.isPending}
        onClick={() => create.mutate({ rating, comment: text })}
      >
        {t.form.submit}
      </Button>
    </div>
  )
}
