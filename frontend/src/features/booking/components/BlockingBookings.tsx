import { Link } from 'react-router'
import { isProblem } from '@/api/problem'
import { formatPeriod } from '@/features/booking/dates'
import type { BookingStatus } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

/** A booking as the backend lists it in a 409 answer that the booking blocks (FR-3c, FR-5, FR-8). */
interface BookingRef {
  id: number
  equipmentId: number
  equipmentTitle: string
  dateFrom: string
  dateTo: string
  status: BookingStatus
}

/** The `bookings` listed in a problem response, as links to the listings; renders nothing when there are none. */
export function BlockingBookings({ error }: { error: unknown }) {
  const bookings = isProblem(error) ? (error.problem.bookings as BookingRef[] | undefined) : undefined
  if (!bookings?.length) return null
  const t = uk.booking

  return (
    <div className="mt-2 text-sm">
      <p className="font-medium">{t.actions.activeBookings}</p>
      <ul className="mt-1 list-disc pl-5">
        {bookings.map((b) => (
          <li key={b.id}>
            <Link to={`/equipment/${b.equipmentId}`} className="underline">
              {b.equipmentTitle}
            </Link>{' '}
            · {formatPeriod(b.dateFrom, b.dateTo)} · {t.statuses[b.status]}
          </li>
        ))}
      </ul>
    </div>
  )
}
