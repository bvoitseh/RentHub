import { useQuery } from '@tanstack/react-query'
import type { LatLon } from '@/components/map/leaflet'
import { recommendationsApi } from '@/features/recommendations/api/recommendationsApi'

/** Keyed by the signed-in user too, so logging in or out (different city, own listings) refetches. */
export function useRecommendations(userId: number | null, point: LatLon | null) {
  return useQuery({
    queryKey: ['recommendations', userId, point?.lat ?? null, point?.lon ?? null],
    queryFn: ({ signal }) => recommendationsApi.get(point, signal),
    staleTime: 60_000,
  })
}
