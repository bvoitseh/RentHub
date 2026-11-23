import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Spinner } from '@/components/ui/Spinner'
import { CalendarDay } from '@/features/booking/components/CalendarDay'
import { formatMonthTitle, monthBounds, monthGrid, shiftMonth, todayIso } from '@/features/booking/dates'
import { useAvailability } from '@/features/booking/hooks/useBookingQueries'
import type { DateRange } from '@/features/booking/types'
import { problemMessage } from '@/api/problem'
import { uk } from '@/i18n/uk'

const t = uk.booking.calendar

export interface AvailabilityCalendarProps {
  equipmentId: number
  /** Days with fewer free units than this are shown as busy. */
  units: number
  range?: DateRange | null
  /** Earliest day that can be picked (e.g. tomorrow); without `onPick` the calendar is read-only. */
  minDate?: string
  onPick?: (date: string) => void
}

/** FR-9: a month of free units per day, with month navigation and an optional period selection. */
export function AvailabilityCalendar({ equipmentId, units, range, minDate, onPick }: AvailabilityCalendarProps) {
  const thisMonth = `${todayIso().slice(0, 7)}-01`
  const [month, setMonth] = useState(thisMonth)
  const { from, to } = monthBounds(month)
  const { data, isPending, isError, error, isFetching } = useAvailability(equipmentId, from, to)
  const byDate = new Map(data?.days.map((d) => [d.date, d]))
  const earliest = minDate ?? todayIso()
  const last = range?.to ?? range?.from

  return (
    <div className="flex w-full max-w-md flex-col gap-2">
      <div className="flex items-center justify-between gap-2">
        <Button variant="ghost" aria-label={t.prev} disabled={month <= thisMonth} onClick={() => setMonth(shiftMonth(month, -1))}>
          ←
        </Button>
        <p className="flex items-center gap-2 font-medium text-slate-900">
          {formatMonthTitle(month)} {isFetching && <Spinner size="sm" />}
        </p>
        <Button variant="ghost" aria-label={t.next} onClick={() => setMonth(shiftMonth(month, 1))}>
          →
        </Button>
      </div>
      {isError ? (
        <p className="text-sm text-red-700">{problemMessage(error)}</p>
      ) : (
        <div className="grid grid-cols-7 gap-1" aria-busy={isPending || undefined}>
          {t.weekdays.map((w) => (
            <span key={w} className="text-center text-xs font-medium text-slate-500">
              {w}
            </span>
          ))}
          {monthGrid(month).map((date, i) =>
            date === null ? (
              <span key={`lead-${i}`} />
            ) : (
              <CalendarDay
                key={date}
                date={date}
                info={byDate.get(date)}
                units={units}
                selectable={date >= earliest}
                selected={Boolean(range && last && date >= range.from && date <= last)}
                edge={date === range?.from || date === range?.to}
                showCount={(data?.quantity ?? 1) > 1}
                onPick={onPick}
              />
            ),
          )}
        </div>
      )}
      <div className="flex flex-wrap gap-3 text-xs text-slate-600">
        <span><span className="mr-1 inline-block h-3 w-3 rounded-sm bg-emerald-100 align-middle" />{t.legendFree}</span>
        <span><span className="mr-1 inline-block h-3 w-3 rounded-sm bg-slate-200 align-middle" />{t.legendBusy}</span>
        {onPick && <span><span className="mr-1 inline-block h-3 w-3 rounded-sm bg-brand-600 align-middle" />{t.legendSelected}</span>}
      </div>
    </div>
  )
}
