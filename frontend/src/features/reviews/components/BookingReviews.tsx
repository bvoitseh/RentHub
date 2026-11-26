import { problemMessage } from '@/api/problem'
import { Spinner } from '@/components/ui/Spinner'
import { ReviewCard } from '@/features/reviews/components/ReviewCard'
import { ReviewForm } from '@/features/reviews/components/ReviewForm'
import { useBookingReviews } from '@/features/reviews/hooks/useReviews'
import { uk } from '@/i18n/uk'

const t = uk.reviews

/**
 * FR-18 for one COMPLETED booking, seen by one of its sides: the review form while the window is open, then the
 * caller's own review (marked hidden until published) and the other side's once it is published.
 */
export function BookingReviews({ bookingId }: { bookingId: number }) {
  const { data, isPending, isError, error } = useBookingReviews(bookingId)

  if (isPending) return <Spinner size="sm" />
  if (isError) return <p className="text-sm text-red-700">{problemMessage(error)}</p>

  return (
    <div className="mt-1 flex flex-col gap-2">
      {data.canReview && <ReviewForm bookingId={bookingId} until={data.reviewUntil} />}
      {data.mine && <ReviewCard review={data.mine} title={t.mine} />}
      {data.theirs && <ReviewCard review={data.theirs} title={t.theirs} />}
      {data.mine && !data.theirs && <p className="text-xs text-slate-500">{t.waitingOther}</p>}
      {!data.mine && !data.canReview && <p className="text-xs text-slate-500">{t.closed}</p>}
    </div>
  )
}
