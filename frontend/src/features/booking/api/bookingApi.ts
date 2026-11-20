import { api } from '@/api/client'
import type { Page } from '@/features/catalog/types'
import type {
  Availability,
  AvailabilityBlock,
  BlockPayload,
  Booking,
  BookingPayload,
  BookingStatus,
  Contact,
} from '@/features/booking/types'

/** Availability (FR-9), booking requests and decisions (FR-10…FR-13a), contacts (FR-3b), calendar blocks (FR-8). */
export const bookingApi = {
  availability: (equipmentId: number, from: string, to: string, signal?: AbortSignal) =>
    api.get<Availability>(`equipment/${equipmentId}/availability`, { from, to }, signal),
  create: (payload: BookingPayload) => api.post<Booking>('bookings', payload),
  mine: (page: number, signal?: AbortSignal) => api.get<Page<Booking>>('bookings/my', { page }, signal),
  incoming: (status: BookingStatus | undefined, page: number, signal?: AbortSignal) =>
    api.get<Page<Booking>>('bookings/incoming', { status, page }, signal),
  confirm: (id: number) => api.post<Booking>(`bookings/${id}/confirm`),
  reject: (id: number) => api.post<Booking>(`bookings/${id}/reject`),
  cancel: (id: number, reason?: string) => api.post<Booking>(`bookings/${id}/cancel`, { reason }),
  contact: (id: number, signal?: AbortSignal) => api.get<Contact>(`bookings/${id}/contact`, undefined, signal),
  blocks: (equipmentId: number, signal?: AbortSignal) =>
    api.get<AvailabilityBlock[]>(`equipment/${equipmentId}/blocks`, undefined, signal),
  addBlock: (equipmentId: number, payload: BlockPayload) =>
    api.post<AvailabilityBlock>(`equipment/${equipmentId}/blocks`, payload),
  removeBlock: (equipmentId: number, blockId: number) => api.delete(`equipment/${equipmentId}/blocks/${blockId}`),
}
