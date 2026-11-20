import type { UseQueryResult } from '@tanstack/react-query'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { Pagination } from '@/features/catalog/components/Pagination'
import type { Page } from '@/features/catalog/types'
import { BookingActions } from '@/features/booking/components/BookingActions'
import { BookingRow } from '@/features/booking/components/BookingRow'
import type { Booking } from '@/features/booking/types'

export interface BookingListProps {
  query: UseQueryResult<Page<Booking>>
  /** The caller's side in these bookings. */
  as: 'owner' | 'renter'
  emptyTitle: string
  emptyHint: string
  onPage: (page: number) => void
}

/** A page of bookings with loading, error and empty states (NFR-6) and pagination. */
export function BookingList({ query, as, emptyTitle, emptyHint, onPage }: BookingListProps) {
  const { data, isPending, isError, error, refetch } = query
  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  if (data.items.length === 0) return <EmptyState title={emptyTitle} description={emptyHint} />

  return (
    <div className="flex flex-col gap-4">
      <ul className="flex flex-col gap-3">
        {data.items.map((b) => (
          <BookingRow
            key={b.id}
            booking={b}
            party={as === 'renter' ? 'owner' : 'renter'}
            actions={<BookingActions booking={b} as={as} />}
          />
        ))}
      </ul>
      <Pagination page={data.page} totalPages={data.totalPages} onChange={onPage} />
    </div>
  )
}
