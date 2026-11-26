import { useState } from 'react'
import { Link } from 'react-router'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { Pagination } from '@/features/catalog/components/Pagination'
import { ReviewCard } from '@/features/reviews/components/ReviewCard'
import { useUserReviews } from '@/features/reviews/hooks/useReviews'
import { uk } from '@/i18n/uk'

const t = uk.reviews

export interface UserReviewsProps {
  userId: number
  title?: string
  /** Show only the first {@code limit} reviews with a link to the profile (equipment card), no paging. */
  limit?: number
}

/** FR-19: a user's published reviews with replies, newest first; the target can reply from here. */
export function UserReviews({ userId, title = t.title, limit }: UserReviewsProps) {
  const [page, setPage] = useState(0)
  const { data, isPending, isError, error, refetch } = useUserReviews(userId, limit ? 0 : page)

  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
      {isPending ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState error={error} onRetry={() => void refetch()} />
      ) : data.items.length === 0 ? (
        <p className="text-sm text-slate-500">{t.empty}</p>
      ) : (
        <>
          {data.items.slice(0, limit ?? data.items.length).map((r) => (
            <ReviewCard key={r.id} review={r} showListing />
          ))}
          {limit ? (
            data.totalItems > limit && (
              <Link to={`/users/${userId}`} className="text-sm font-medium text-brand-700 hover:underline">
                {t.all} ({data.totalItems})
              </Link>
            )
          ) : (
            <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
          )}
        </>
      )}
    </section>
  )
}
