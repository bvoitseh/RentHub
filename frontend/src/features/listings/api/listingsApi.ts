import { api } from '@/api/client'
import type { EquipmentDetails, EquipmentSummary, Photo } from '@/features/catalog/types'
import type { ListingLocation, ListingPayload, PinAddress } from '@/features/listings/types'

/** The owner's listing endpoints (FR-4, FR-4a, FR-5) and the geocoding preview for the pin. */
export const listingsApi = {
  mine: (signal?: AbortSignal) => api.get<EquipmentSummary[]>('equipment/my', undefined, signal),
  create: (payload: ListingPayload) => api.post<EquipmentDetails>('equipment', payload),
  update: (id: number, payload: ListingPayload) => api.put<EquipmentDetails>(`equipment/${id}`, payload),
  archive: (id: number) => api.post<EquipmentDetails>(`equipment/${id}/archive`),
  remove: (id: number) => api.delete(`equipment/${id}`),
  uploadPhoto: (id: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return api.post<Photo[]>(`equipment/${id}/photos`, form)
  },
  removePhoto: (id: number, photoId: number) => api.delete<Photo[]>(`equipment/${id}/photos/${photoId}`),
  locate: (settlementId: number, street: string) => api.get<ListingLocation>('geocode', { settlementId, street }),
  describe: (settlementId: number, lat: number, lon: number) =>
    api.get<PinAddress>('geocode/reverse', { settlementId, lat, lon }),
}
