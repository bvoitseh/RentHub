import { Link } from 'react-router'
import { OwnerLabel } from '@/features/catalog/components/OwnerLabel'
import type { OwnerBadge } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog.card

/** The owner on the card: name, account type, rating from published reviews (FR-19), link to the profile. */
export function OwnerPanel({ owner }: { owner: OwnerBadge }) {
  return (
    <section className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="text-sm font-medium text-slate-500">{t.owner}</h2>
      <p className="font-semibold text-slate-900">{owner.name}</p>
      <div className="flex flex-wrap items-center gap-2 text-sm text-slate-600">
        <OwnerLabel ownerType={owner.ownerType} companyName={owner.companyName} />
        {owner.city && <span>{owner.city}</span>}
      </div>
      <p className="text-sm text-slate-600">
        {owner.rating !== null ? t.rating(owner.rating, owner.reviewCount) : t.noRating}
      </p>
      <Link to={`/users/${owner.id}`} className="text-sm font-medium text-brand-700 hover:underline">
        {t.ownerProfile}
      </Link>
    </section>
  )
}
