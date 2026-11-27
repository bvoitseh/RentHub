import { api } from '@/api/client'
import type { Region, Settlement } from '@/features/address/types'

/** D-48: answered from our own database, never by an external geocoder. */
export const addressApi = {
  regions: (signal?: AbortSignal) => api.get<Region[]>('regions', undefined, signal),
  settlements: (regionId: number, q: string, signal?: AbortSignal) =>
    api.get<Settlement[]>('settlements', { regionId, q }, signal),
}
