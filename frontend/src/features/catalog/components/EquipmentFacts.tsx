import type { EquipmentDetails } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog.card

/** Condition, category, units, rental period, handover and address (FR-4b, FR-4c). */
export function EquipmentFacts({ item }: { item: EquipmentDetails }) {
  const handover = [item.pickupAvailable && t.pickup, item.deliveryAvailable && t.delivery].filter(Boolean).join(', ')
  const rows: [string, string][] = [
    [t.condition, uk.catalog.conditions[item.condition]],
    [t.category, item.category.name],
    [t.quantity, String(item.quantity)],
    [t.rentalTerms, t.days(item.minDays, item.maxDays)],
    [t.handover, handover],
  ]
  if (item.deliveryNote) rows.push([t.deliveryNote, item.deliveryNote])
  rows.push([t.address, item.address])

  return (
    <dl className="grid grid-cols-1 gap-x-6 gap-y-3 sm:grid-cols-[max-content_1fr]">
      {rows.map(([label, value]) => (
        <div key={label} className="contents">
          <dt className="text-sm text-slate-500">{label}</dt>
          <dd className="text-sm font-medium text-slate-900">{value}</dd>
        </div>
      ))}
    </dl>
  )
}
