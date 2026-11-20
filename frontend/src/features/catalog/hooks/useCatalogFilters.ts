import { useCallback, useMemo } from 'react'
import { useSearchParams } from 'react-router'
import type { CatalogFilters } from '@/features/catalog/types'

const KEYS: (keyof CatalogFilters)[] = [
  'q', 'categoryId', 'priceMin', 'priceMax', 'dateFrom', 'dateTo', 'units',
  'lat', 'lon', 'radiusKm', 'pickup', 'delivery', 'ownerType', 'sort', 'page',
]

/**
 * FR-7 filters live in the URL (shareable, back/forward friendly). `update` merges changes and resets the page
 * unless the page itself is being changed; empty values are removed.
 */
export function useCatalogFilters() {
  const [params, setParams] = useSearchParams()

  const filters = useMemo(() => {
    const result: Record<string, string> = {}
    for (const key of KEYS) {
      const value = params.get(key)
      if (value) result[key] = value
    }
    return result as CatalogFilters
  }, [params])

  const update = useCallback(
    (changes: Partial<CatalogFilters>) => {
      setParams((current) => {
        const next = new URLSearchParams(current)
        for (const [key, value] of Object.entries(changes)) {
          if (value === undefined || value === '') next.delete(key)
          else next.set(key, String(value))
        }
        if (!('page' in changes)) next.delete('page')
        return next
      })
    },
    [setParams],
  )

  const reset = useCallback(() => setParams(new URLSearchParams()), [setParams])

  return { filters, update, reset }
}
