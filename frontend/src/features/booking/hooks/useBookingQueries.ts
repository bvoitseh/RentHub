import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { bookingApi } from '@/features/booking/api/bookingApi'
import type { BookingStatus } from '@/features/booking/types'

export const bookingKeys = {
  all: ['bookings'] as const,
  availability: (id: number, from: string, to: string) => ['bookings', 'availability', id, from, to] as const,
  mine: (page: number) => ['bookings', 'mine', page] as const,
  incoming: (status: BookingStatus | undefined, page: number) => ['bookings', 'incoming', status, page] as const,
  contact: (id: number) => ['bookings', 'contact', id] as const,
  blocks: (equipmentId: number) => ['bookings', 'blocks', equipmentId] as const,
}

export function useAvailability(equipmentId: number, from: string, to: string, enabled = true) {
  return useQuery({
    queryKey: bookingKeys.availability(equipmentId, from, to),
    queryFn: ({ signal }) => bookingApi.availability(equipmentId, from, to, signal),
    placeholderData: keepPreviousData,
    enabled,
  })
}

export function useMyBookings(page: number) {
  return useQuery({
    queryKey: bookingKeys.mine(page),
    queryFn: ({ signal }) => bookingApi.mine(page, signal),
    placeholderData: keepPreviousData,
  })
}

export function useIncomingBookings(status: BookingStatus | undefined, page: number) {
  return useQuery({
    queryKey: bookingKeys.incoming(status, page),
    queryFn: ({ signal }) => bookingApi.incoming(status, page, signal),
    placeholderData: keepPreviousData,
  })
}

/** FR-3b: only asked for once the booking is CONFIRMED or COMPLETED. */
export function useContact(bookingId: number, enabled: boolean) {
  return useQuery({
    queryKey: bookingKeys.contact(bookingId),
    queryFn: ({ signal }) => bookingApi.contact(bookingId, signal),
    enabled,
    staleTime: 5 * 60_000,
  })
}

export function useBlocks(equipmentId: number) {
  return useQuery({
    queryKey: bookingKeys.blocks(equipmentId),
    queryFn: ({ signal }) => bookingApi.blocks(equipmentId, signal),
  })
}
