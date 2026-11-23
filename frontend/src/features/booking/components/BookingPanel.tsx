import { useState } from 'react'
import { Link, useLocation } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { AvailabilityCalendar } from '@/features/booking/components/AvailabilityCalendar'
import { BookingForm } from '@/features/booking/components/BookingForm'
import { addDays, todayIso } from '@/features/booking/dates'
import { useDateRange } from '@/features/booking/hooks/useDateRange'
import type { EquipmentDetails } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.booking.form
const linkClass = 'self-start rounded-md bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700'

/**
 * The card's booking block: availability calendar (FR-9) and, for a verified user who is not the owner, the booking
 * form in three steps — pick the period, choose units and handover, send (NFR-6).
 */
export function BookingPanel({ item }: { item: EquipmentDetails }) {
  const location = useLocation()
  const { data: user } = useCurrentUser()
  const { range, pick, clear } = useDateRange()
  const [units, setUnits] = useState(1)
  const [sent, setSent] = useState(false)
  const canBook = user?.role === 'USER' && user.emailVerified && !item.mine && item.status === 'ACTIVE'

  return (
    <section className="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="text-lg font-semibold text-slate-900">{canBook ? t.title : uk.booking.calendar.title}</h2>
      <p className="text-sm text-slate-600">{t.terms(item.minDays, item.maxDays)}</p>
      <AvailabilityCalendar
        equipmentId={item.id}
        units={units}
        range={canBook ? range : null}
        minDate={addDays(todayIso(), 1)}
        onPick={canBook ? (day) => (setSent(false), pick(day)) : undefined}
      />
      {sent && (
        <Alert tone="success">
          {t.sent}{' '}
          <Link to="/my/bookings" className="font-medium underline">
            {t.toMine}
          </Link>
        </Alert>
      )}
      {item.mine ? (
        <p className="text-sm text-slate-600">{t.ownListing}</p>
      ) : !user ? (
        <Link to="/login" state={{ from: location.pathname }} className={linkClass}>
          {t.loginToBook}
        </Link>
      ) : !user.emailVerified ? (
        <Alert tone="warning">{t.verifyToBook}</Alert>
      ) : (
        canBook && (
          <BookingForm
            key={`${range?.from}-${range?.to}`}
            item={item}
            range={range}
            units={units}
            onUnitsChange={setUnits}
            onClear={clear}
            onSent={() => {
              setSent(true)
              clear()
            }}
          />
        )
      )}
    </section>
  )
}
