import { formatPrice } from '@/features/catalog/format'
import { formatPeriod } from '@/features/booking/dates'
import { uk } from '@/i18n/uk'

const t = uk.booking.form

export interface PriceSummaryProps {
  from: string
  to: string
  days: number
  units: number
  pricePerDay: number
}

/** FR-10 instant price: days × units × price per day. */
export function PriceSummary({ from, to, days, units, pricePerDay }: PriceSummaryProps) {
  return (
    <div className="flex flex-col gap-1 rounded-md bg-slate-50 p-3 text-sm">
      <p className="text-slate-700">
        {formatPeriod(from, to)} · {t.days(days)}
      </p>
      <p className="text-slate-500">{t.calc(days, units, formatPrice(pricePerDay))}</p>
      <p className="text-lg font-bold text-slate-900">
        {t.total}: {formatPrice(days * units * pricePerDay)}
      </p>
    </div>
  )
}
