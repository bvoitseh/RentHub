import { useState } from 'react'
import { Link } from 'react-router'
import type { LatLon } from '@/components/map/leaflet'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { LocationControl } from '@/features/recommendations/components/LocationControl'
import { RecommendationCard } from '@/features/recommendations/components/RecommendationCard'
import { useRecommendations } from '@/features/recommendations/hooks/useRecommendations'
import { uk } from '@/i18n/uk'

const t = uk.recommendations

/** FR-16, FR-17 on the home page: equipment near the user (or the city), nearest first, «Сезон» on in-season ones. */
export function Recommendations() {
  const { data: user } = useCurrentUser()
  const [point, setPoint] = useState<LatLon | null>(null)
  const usable = user?.geoConsent ? point : null
  const { data, isPending, isError, error, refetch } = useRecommendations(user?.id ?? null, usable)

  return (
    <section className="flex w-full flex-col gap-4 text-left">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">{t.title}</h2>
          {data && (
            <p className="text-sm text-slate-600">
              {t.area(data.origin.source === 'BROWSER' ? t.yourLocation : (data.origin.label ?? ''))}
            </p>
          )}
        </div>
        <LocationControl point={usable} onChange={setPoint} />
      </div>
      {isPending ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState error={error} onRetry={() => void refetch()} />
      ) : data.items.length === 0 ? (
        <EmptyState
          title={t.empty}
          description={t.emptyHint}
          action={<Link to="/catalog" className="text-sm font-medium text-brand-700 hover:underline">{t.toCatalog}</Link>}
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.items.map((item) => (
            <RecommendationCard key={item.equipment.id} item={item} fromUser={data.origin.source === 'BROWSER'} />
          ))}
        </div>
      )}
    </section>
  )
}
