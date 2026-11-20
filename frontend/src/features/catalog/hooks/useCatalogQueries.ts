import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { catalogApi } from '@/features/catalog/api/catalogApi'
import type { CatalogFilters } from '@/features/catalog/types'

export const catalogKeys = {
  categories: ['catalog', 'categories'] as const,
  search: (filters: CatalogFilters) => ['catalog', 'search', filters] as const,
  card: (id: number) => ['catalog', 'card', id] as const,
  profile: (id: number) => ['catalog', 'profile', id] as const,
}

export function useCategories() {
  return useQuery({
    queryKey: catalogKeys.categories,
    queryFn: ({ signal }) => catalogApi.categories(signal),
    staleTime: 30 * 60_000,
  })
}

/** Keeps the previous page on screen while the next filter combination loads. */
export function useCatalogSearch(filters: CatalogFilters) {
  return useQuery({
    queryKey: catalogKeys.search(filters),
    queryFn: ({ signal }) => catalogApi.search(filters, signal),
    placeholderData: keepPreviousData,
  })
}

export function useEquipmentCard(id: number) {
  return useQuery({
    queryKey: catalogKeys.card(id),
    queryFn: ({ signal }) => catalogApi.card(id, signal),
    enabled: Number.isFinite(id),
  })
}

export function usePublicProfile(id: number) {
  return useQuery({
    queryKey: catalogKeys.profile(id),
    queryFn: ({ signal }) => catalogApi.profile(id, signal),
    enabled: Number.isFinite(id),
  })
}
