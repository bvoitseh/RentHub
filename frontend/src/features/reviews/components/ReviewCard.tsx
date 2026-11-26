import { Link } from 'react-router'
import { formatDateTime } from '@/features/booking/dates'
import { ReplyForm } from '@/features/reviews/components/ReplyForm'
import { Stars } from '@/features/reviews/components/Stars'
import type { Review } from '@/features/reviews/types'
import { uk } from '@/i18n/uk'

const t = uk.reviews

export interface ReviewCardProps {
  review: Review
  /** Heading override, e.g. «Ваш відгук»; by default the author's name. */
  title?: string
  /** Show which listing the rental was about (on profiles). */
  showListing?: boolean
}

/** One review: stars, author (or «деактивовано», FR-3c), date, text, the listing, the reply or the reply form. */
export function ReviewCard({ review: r, title, showListing }: ReviewCardProps) {
  return (
    <article className="flex flex-col gap-1 rounded-md border border-slate-200 bg-white p-3 text-sm">
      <div className="flex flex-wrap items-center gap-2">
        <Stars value={r.rating} />
        <span className="font-medium text-slate-900">
          {title ?? (r.author.deactivated ? t.deactivated : (
            <Link to={`/users/${r.author.id}`} className="hover:underline">
              {r.author.name}
            </Link>
          ))}
        </span>
        <span className="text-xs text-slate-500">{formatDateTime(r.publishedAt ?? r.createdAt)}</span>
        {!r.published && <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-800">{t.pending}</span>}
      </div>
      <p className="whitespace-pre-line text-slate-700">{r.comment}</p>
      {showListing && (
        <Link to={`/equipment/${r.equipment.id}`} className="text-xs text-slate-500 hover:underline">
          {r.equipment.title}
        </Link>
      )}
      {r.reply && (
        <div className="mt-1 rounded-md bg-slate-50 p-2">
          <p className="text-xs font-medium text-slate-600">
            {t.reply.label} · {r.target.deactivated ? t.deactivated : r.target.name}
          </p>
          <p className="whitespace-pre-line text-slate-700">{r.reply}</p>
        </div>
      )}
      {r.canReply && <ReplyForm reviewId={r.id} />}
    </article>
  )
}
