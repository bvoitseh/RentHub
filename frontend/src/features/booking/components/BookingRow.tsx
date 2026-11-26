import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { EquipmentCover } from '@/features/catalog/components/EquipmentCover'
import { formatPrice } from '@/features/catalog/format'
import { BookingContact } from '@/features/booking/components/BookingContact'
import { BookingStatusBadge } from '@/features/booking/components/BookingStatusBadge'
import { BookingReviews } from '@/features/reviews/components/BookingReviews'
import { formatDateTime, formatPeriod } from '@/features/booking/dates'
import type { Booking } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

const t = uk.booking

export interface BookingRowProps {
  booking: Booking
  /** Whose name to show next to the listing: the owner for the renter's list, the renter for incoming requests. */
  party: 'owner' | 'renter'
  actions?: ReactNode
}

/** One booking in a list: listing, period, units, handover, total, status, deadline, reason, contact and actions. */
export function BookingRow({ booking: b, party, actions }: BookingRowProps) {
  const other = party === 'owner' ? b.owner : b.renter
  return (
    <li className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white p-3 sm:flex-row">
      <Link to={`/equipment/${b.equipment.id}`} className="w-full shrink-0 overflow-hidden rounded-md sm:w-32">
        <EquipmentCover url={b.equipment.coverUrl} alt={b.equipment.title} />
      </Link>
      <div className="flex min-w-0 flex-1 flex-col gap-1 text-sm">
        <div className="flex flex-wrap items-center gap-2">
          <Link to={`/equipment/${b.equipment.id}`} className="truncate font-semibold text-slate-900 hover:text-brand-700">
            {b.equipment.title}
          </Link>
          <BookingStatusBadge booking={b} />
        </div>
        <p className="text-slate-700">
          {formatPeriod(b.dateFrom, b.dateTo)} · {t.form.days(b.days)} · {t.list.units(b.units)} ·{' '}
          {b.handover === 'PICKUP' ? t.form.pickup : t.form.delivery}
        </p>
        <p className="text-slate-700">
          {party === 'owner' ? t.list.owner : t.list.renter}:{' '}
          <Link to={`/users/${other.id}`} className="text-brand-700 hover:underline">
            {other.name}
          </Link>{' '}
          <span className="text-slate-500">
            {other.rating !== null ? uk.reviews.rating(other.rating, other.reviewCount) : uk.reviews.noRating}
          </span>
        </p>
        <p className="font-semibold text-slate-900">
          {t.form.total}: {formatPrice(b.totalPrice)}
          {party === 'renter' && (
            <span className="ml-2 font-normal text-slate-500">{t.list.commission(formatPrice(b.commission))}</span>
          )}
        </p>
        {b.respondBy && (
          <p className="text-amber-800">
            {(party === 'renter' ? t.list.respondBy : t.list.ownerRespondsBy)(formatDateTime(b.respondBy))}
          </p>
        )}
        {b.cancelReason && (
          <p className="text-slate-600">
            {t.list.reason}: {b.cancelReason}
          </p>
        )}
        <BookingContact booking={b} />
        {b.status === 'COMPLETED' && <BookingReviews bookingId={b.id} />}
      </div>
      {actions && <div className="flex shrink-0 flex-wrap items-start gap-2 sm:flex-col">{actions}</div>}
    </li>
  )
}
