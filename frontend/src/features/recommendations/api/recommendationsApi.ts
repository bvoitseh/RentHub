import { api } from '@/api/client'
import type { LatLon } from '@/components/map/leaflet'
import type { Recommendations } from '@/features/recommendations/types'

/** GET /recommendations; browser coordinates are sent only when the user chose to use them (never stored). */
export const recommendationsApi = {
  get: (point: LatLon | null, signal?: AbortSignal) =>
    api.get<Recommendations>('recommendations', point ? { lat: point.lat, lon: point.lon } : undefined, signal),
}
