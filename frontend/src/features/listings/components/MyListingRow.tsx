import { EquipmentCover } from '@/features/catalog/components/EquipmentCover'
import { formatPrice } from '@/features/catalog/format'
import type { EquipmentSummary } from '@/features/catalog/types'
import { ListingActions } from '@/features/listings/components/ListingActions'
import { uk } from '@/i18n/uk'

const t = uk.listings
const statusClass = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  ARCHIVED: 'bg-slate-100 text-slate-600',
  BLOCKED: 'bg-red-50 text-red-700',
} as const

/** One listing on the owner's dashboard: cover, title, price, status and actions. */
export function MyListingRow({ item }: { item: EquipmentSummary }) {
  return (
    <li className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-surface p-3 sm:flex-row sm:items-center">
      <div className="w-full shrink-0 overflow-hidden rounded-md sm:w-32">
        <EquipmentCover url={item.coverUrl} alt={item.title} />
      </div>
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <p className="truncate font-semibold text-slate-900">{item.title}</p>
        <p className="text-sm text-slate-600">
          {formatPrice(item.pricePerDay)} {uk.catalog.perDay} · {item.categoryName}
        </p>
        <div className="flex flex-wrap gap-2 text-xs">
          <span className={`rounded-full px-2 py-0.5 font-medium ${statusClass[item.status]}`}>{t.statuses[item.status]}</span>
          {!item.coverUrl && <span className="rounded-full bg-amber-50 px-2 py-0.5 font-medium text-amber-700">{t.noPhotos}</span>}
        </div>
      </div>
      <ListingActions item={item} />
    </li>
  )
}
