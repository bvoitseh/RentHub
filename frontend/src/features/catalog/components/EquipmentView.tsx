import { Link } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { MapView } from '@/components/map/MapView'
import { BookingPanel } from '@/features/booking/components/BookingPanel'
import { ComplaintButton } from '@/features/complaints/components/ComplaintButton'
import { UserReviews } from '@/features/reviews/components/UserReviews'
import { EquipmentFacts } from '@/features/catalog/components/EquipmentFacts'
import { EquipmentGallery } from '@/features/catalog/components/EquipmentGallery'
import { OwnerPanel } from '@/features/catalog/components/OwnerPanel'
import { formatPrice } from '@/features/catalog/format'
import type { EquipmentDetails } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog.card

/** The equipment card (FR-6): gallery, price, facts, owner, booking, description, map and the owner's reviews. */
export function EquipmentView({ item }: { item: EquipmentDetails }) {
  const hidden = item.status !== 'ACTIVE' || item.photos.length === 0

  return (
    <article className="flex flex-col gap-6">
      {item.mine && hidden && (
        <Alert tone="info">
          {item.status !== 'ACTIVE' ? `${t.statusNote}: ${uk.listings.statuses[item.status]}` : t.noPhotosNote}
        </Alert>
      )}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[3fr_2fr]">
        <EquipmentGallery photos={item.photos} title={item.title} />
        <div className="flex flex-col gap-4">
          <h1 className="text-2xl font-bold text-slate-900">{item.title}</h1>
          <p className="text-2xl font-bold text-slate-900">
            {formatPrice(item.pricePerDay)} <span className="text-base font-normal text-slate-500">{uk.catalog.perDay}</span>
          </p>
          <EquipmentFacts item={item} />
          {item.mine && (
            <Link
              to={`/my/listings/${item.id}/edit`}
              className="self-start rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-800 hover:bg-slate-50"
            >
              {t.edit}
            </Link>
          )}
          <OwnerPanel owner={item.owner} />
          {!item.mine && <ComplaintButton subject={{ equipmentId: item.id }} />}
        </div>
      </div>
      <BookingPanel item={item} />
      <section className="flex flex-col gap-2">
        <p className="whitespace-pre-line text-slate-700">{item.description}</p>
      </section>
      <MapView lat={item.lat} lon={item.lon} label={uk.catalog.map.pin} />
      <UserReviews userId={item.owner.id} title={uk.reviews.ownerTitle} limit={3} />
    </article>
  )
}
