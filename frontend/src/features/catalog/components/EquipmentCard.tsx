import { Link } from 'react-router'
import { EquipmentCover } from '@/features/catalog/components/EquipmentCover'
import { OwnerLabel } from '@/features/catalog/components/OwnerLabel'
import { formatPrice } from '@/features/catalog/format'
import type { EquipmentSummary } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog

/** Catalogue card: cover, title, price per day, category, place or distance, owner label, handover options. */
export function EquipmentCard({ item }: { item: EquipmentSummary }) {
  return (
    <Link
      to={`/equipment/${item.id}`}
      className="group flex flex-col overflow-hidden rounded-lg border border-slate-200 bg-white transition-shadow hover:shadow-md focus-visible:outline-2 focus-visible:outline-brand-600"
    >
      <EquipmentCover url={item.coverUrl} alt={item.title} />
      <div className="flex flex-1 flex-col gap-2 p-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{item.categoryName}</p>
        <h3 className="line-clamp-2 font-semibold text-slate-900 group-hover:text-brand-700">{item.title}</h3>
        <p className="text-lg font-bold text-slate-900">
          {formatPrice(item.pricePerDay)} <span className="text-sm font-normal text-slate-500">{t.perDay}</span>
        </p>
        <p className="line-clamp-1 text-sm text-slate-600">
          {item.distanceKm !== null ? t.distance(item.distanceKm) : item.address}
        </p>
        <div className="mt-auto flex flex-wrap items-center gap-2 pt-1 text-xs text-slate-600">
          <OwnerLabel ownerType={item.owner.ownerType} companyName={item.owner.companyName} />
          {item.pickupAvailable && <span>{t.card.pickup}</span>}
          {item.deliveryAvailable && <span>{t.card.delivery}</span>}
          {item.quantity > 1 && <span>{t.units(item.quantity)}</span>}
        </div>
      </div>
    </Link>
  )
}
