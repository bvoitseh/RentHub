import { problemMessage } from '@/api/problem'
import { Spinner } from '@/components/ui/Spinner'
import { useContact } from '@/features/booking/hooks/useBookingQueries'
import type { Booking } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

/** FR-3b: the other party's name and phone, shown only for CONFIRMED and COMPLETED bookings. */
export function BookingContact({ booking }: { booking: Booking }) {
  const visible = booking.status === 'CONFIRMED' || booking.status === 'COMPLETED'
  const { data, isPending, isError, error } = useContact(booking.id, visible)
  if (!visible) return null
  const t = uk.booking.contact

  return (
    <div className="rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-900">
      <span className="font-medium">{t.title}: </span>
      {isPending ? (
        <Spinner size="sm" />
      ) : isError ? (
        problemMessage(error)
      ) : (
        <>
          {data.name}
          {data.phone && (
            <>
              {' · '}
              <a href={`tel:${data.phone}`} className="font-medium underline">
                {data.phone}
              </a>
            </>
          )}
        </>
      )}
    </div>
  )
}
