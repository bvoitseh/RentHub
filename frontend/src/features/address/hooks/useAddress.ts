import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { addressApi } from '@/features/address/api/addressApi'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'

export function useRegions() {
  return useQuery({
    queryKey: ['address', 'regions'],
    queryFn: ({ signal }) => addressApi.regions(signal),
    staleTime: 24 * 60 * 60_000,
  })
}

/** Suggestions after a short pause in typing, from 2 characters on (D-48). */
export function useSettlementSuggestions(regionId: number | null, text: string) {
  const q = useDebouncedValue(text.trim())
  const enabled = regionId !== null && q.length >= 2
  const query = useQuery({
    queryKey: ['address', 'settlements', regionId, q],
    queryFn: ({ signal }) => addressApi.settlements(regionId as number, q, signal),
    enabled,
    staleTime: 60 * 60_000,
    placeholderData: keepPreviousData,
  })
  return { ...query, enabled }
}
