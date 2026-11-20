import { useState } from 'react'
import { Select } from '@/components/ui/Select'
import { BookingList } from '@/features/booking/components/BookingList'
import { useIncomingBookings } from '@/features/booking/hooks/useBookingQueries'
import type { BookingStatus } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

const t = uk.booking
const statuses: BookingStatus[] = ['NEW', 'CONFIRMED', 'COMPLETED', 'REJECTED', 'CANCELLED']

/** FR-12, FR-13a, FR-15b: requests for the owner's listings, with their deadlines and decisions. */
export function IncomingBookingsPage() {
  const [status, setStatus] = useState<BookingStatus | undefined>('NEW')
  const [page, setPage] = useState(0)
  const query = useIncomingBookings(status, page)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">{t.list.incomingTitle}</h1>
        <Select
          aria-label={uk.catalog.filters.title}
          className="w-auto"
          value={status ?? ''}
          placeholder={t.list.all}
          options={statuses.map((s) => ({ value: s, label: t.statuses[s] }))}
          onChange={(e) => {
            setStatus((e.target.value || undefined) as BookingStatus | undefined)
            setPage(0)
          }}
        />
      </div>
      <BookingList
        query={query}
        as="owner"
        emptyTitle={t.list.incomingEmpty}
        emptyHint={t.list.incomingEmptyHint}
        onPage={setPage}
      />
    </div>
  )
}
