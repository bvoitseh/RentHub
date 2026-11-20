import { Alert } from '@/components/ui/Alert'
import { OwnerLabel } from '@/features/catalog/components/OwnerLabel'
import { formatMonth } from '@/features/catalog/format'
import type { PublicProfile } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog.profile

/** FR-19 public profile: name, city, account type, rating, review/cancellation/complaint counts; no contacts. */
export function PublicProfileCard({ profile }: { profile: PublicProfile }) {
  const stats: [string, string][] = [
    [t.rating, profile.rating !== null ? `★ ${profile.rating.toFixed(1)}` : '—'],
    [t.reviews, String(profile.reviewCount)],
    [t.cancellations, String(profile.ownerCancellations)],
    [t.complaints, String(profile.complaintCount)],
  ]

  return (
    <section className="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-6">
      {profile.deactivated && <Alert tone="warning">{t.deactivated}</Alert>}
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-slate-900">{profile.name}</h1>
        <div className="flex flex-wrap items-center gap-2 text-sm text-slate-600">
          <OwnerLabel ownerType={profile.ownerType} companyName={profile.companyName} />
          {profile.city && <span>{profile.city}</span>}
          <span>{t.memberSince(formatMonth(profile.memberSince))}</span>
        </div>
      </div>
      <dl className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {stats.map(([label, value]) => (
          <div key={label} className="rounded-md bg-slate-50 p-3 text-center">
            <dt className="text-xs text-slate-500">{label}</dt>
            <dd className="text-lg font-semibold text-slate-900">{value}</dd>
          </div>
        ))}
      </dl>
    </section>
  )
}
