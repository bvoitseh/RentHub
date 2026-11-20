import type { AvailabilityDay } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

export interface CalendarDayProps {
  date: string
  info: AvailabilityDay | undefined
  units: number
  selectable: boolean
  selected: boolean
  edge: boolean
  /** Show the number of free units (listings with more than one unit). */
  showCount: boolean
  onPick?: (date: string) => void
}

/** One day cell: free units, closed / busy / selected styling; disabled in the past or when not selectable. */
export function CalendarDay({ date, info, units, selectable, selected, edge, showCount, onPick }: CalendarDayProps) {
  const free = info?.freeUnits ?? 0
  const busy = !info || info.blocked || free < units
  const tone = edge
    ? 'bg-brand-600 text-white'
    : selected
      ? 'bg-brand-100 text-brand-900'
      : busy
        ? 'bg-slate-100 text-slate-400 line-through'
        : 'bg-emerald-50 text-emerald-900 hover:bg-emerald-100'
  const title = info?.blocked ? uk.booking.calendar.blocked : uk.booking.calendar.free(free)

  return (
    <button
      type="button"
      title={title}
      aria-label={`${date}: ${title}`}
      aria-pressed={selected || undefined}
      disabled={!selectable || !onPick}
      onClick={() => onPick?.(date)}
      className={`flex aspect-square flex-col items-center justify-center rounded-md text-sm transition-colors disabled:cursor-default ${tone}`}
    >
      <span className="font-medium">{Number(date.slice(8))}</span>
      {showCount && info && !info.blocked && free > 0 && <span className="text-[10px] leading-none">{free}</span>}
    </button>
  )
}
