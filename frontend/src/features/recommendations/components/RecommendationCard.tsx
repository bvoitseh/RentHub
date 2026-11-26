import { EquipmentCard } from '@/features/catalog/components/EquipmentCard'
import type { Recommendations } from '@/features/recommendations/types'
import { uk } from '@/i18n/uk'

/** A catalogue card with a «Сезон» badge when its category is in season now (FR-17). */
export interface RecommendationCardProps {
  item: Recommendations['items'][number]
  /** The distance is from the user's own location (true) or from the city centre (false). */
  fromUser: boolean
}

export function RecommendationCard({ item, fromUser }: RecommendationCardProps) {
  return (
    <div className="relative">
      <EquipmentCard item={item.equipment} distanceText={fromUser ? undefined : uk.recommendations.fromCentre} />
      {item.inSeason && (
        <span className="absolute left-2 top-2 rounded-full bg-emerald-600 px-2 py-0.5 text-xs font-medium text-white shadow">
          {uk.recommendations.season}
        </span>
      )}
    </div>
  )
}
