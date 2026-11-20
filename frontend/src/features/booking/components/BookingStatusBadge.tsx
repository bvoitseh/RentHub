import type { Booking } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

const tone = {
  NEW: 'bg-amber-50 text-amber-800',
  CONFIRMED: 'bg-emerald-50 text-emerald-700',
  REJECTED: 'bg-slate-100 text-slate-600',
  CANCELLED: 'bg-red-50 text-red-700',
  COMPLETED: 'bg-sky-50 text-sky-700',
} as const

/** Status label; an expired request reads "Минув термін відповіді", a cancellation says by whom. */
export function BookingStatusBadge({ booking }: { booking: Booking }) {
  const t = uk.booking
  const label = booking.expired
    ? t.expired
    : booking.status === 'CANCELLED' && booking.cancelledBy
      ? `${t.statuses.CANCELLED} ${t.cancelledBy[booking.cancelledBy]}`
      : t.statuses[booking.status]
  return <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${tone[booking.status]}`}>{label}</span>
}
