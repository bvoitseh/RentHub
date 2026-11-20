import { useState } from 'react'
import { BookingList } from '@/features/booking/components/BookingList'
import { useMyBookings } from '@/features/booking/hooks/useBookingQueries'
import { uk } from '@/i18n/uk'

const t = uk.booking.list

/** FR-13: the renter's own bookings, with cancellation and the owner's contact after confirmation. */
export function MyBookingsPage() {
  const [page, setPage] = useState(0)
  const query = useMyBookings(page)

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold text-slate-900">{t.mineTitle}</h1>
      <BookingList query={query} as="renter" emptyTitle={t.mineEmpty} emptyHint={t.mineEmptyHint} onPage={setPage} />
    </div>
  )
}
