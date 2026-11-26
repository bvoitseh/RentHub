import type { EquipmentSummary } from '@/features/catalog/types'

export type RecommendationSource = 'BROWSER' | 'CITY' | 'START_CITY'

/** FR-16, FR-17: where the recommendations are near (label = city name, null for the browser location) and the cards. */
export interface Recommendations {
  origin: { source: RecommendationSource; label: string | null }
  items: { equipment: EquipmentSummary; inSeason: boolean }[]
}
