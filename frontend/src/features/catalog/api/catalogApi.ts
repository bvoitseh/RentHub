import { api } from '@/api/client'
import type {
  CatalogFilters,
  Category,
  EquipmentDetails,
  EquipmentSummary,
  Page,
  PublicProfile,
} from '@/features/catalog/types'

/** Public catalogue endpoints (FR-6, FR-7, FR-19); open to guests. */
export const catalogApi = {
  categories: (signal?: AbortSignal) => api.get<Category[]>('categories', undefined, signal),
  search: (filters: CatalogFilters, signal?: AbortSignal) =>
    api.get<Page<EquipmentSummary>>('equipment', { ...filters }, signal),
  card: (id: number, signal?: AbortSignal) => api.get<EquipmentDetails>(`equipment/${id}`, undefined, signal),
  profile: (id: number, signal?: AbortSignal) => api.get<PublicProfile>(`users/${id}`, undefined, signal),
}
