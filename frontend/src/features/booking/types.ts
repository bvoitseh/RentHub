export type Handover = 'PICKUP' | 'DELIVERY'
export type BookingStatus = 'NEW' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED'
export type CancelledBy = 'RENTER' | 'OWNER' | 'ADMIN'

export interface AvailabilityDay {
  date: string
  freeUnits: number
  blocked: boolean
}

/** FR-9: free units of a listing per day of [from, to]. */
export interface Availability {
  equipmentId: number
  quantity: number
  from: string
  to: string
  days: AvailabilityDay[]
}

export interface BookingPayload {
  equipmentId: number
  units: number
  dateFrom: string
  dateTo: string
  handover: Handover
}

/** A side of a booking; rating/reviewCount from published reviews (FR-19), rating null without any. */
export interface BookingParty {
  id: number
  name: string
  rating: number | null
  reviewCount: number
}

export interface Booking {
  id: number
  equipment: { id: number; title: string; coverUrl: string | null }
  renter: BookingParty
  owner: BookingParty
  units: number
  dateFrom: string
  dateTo: string
  days: number
  handover: Handover
  pricePerDay: number
  totalPrice: number
  commission: number
  status: BookingStatus
  expired: boolean
  createdAt: string
  decidedAt: string | null
  /** FR-15b deadline, NEW requests only. */
  respondBy: string | null
  cancelledBy: CancelledBy | null
  cancelReason: string | null
}

export interface Contact {
  userId: number
  name: string
  phone: string | null
}

export interface AvailabilityBlock {
  id: number
  dateFrom: string
  dateTo: string
  reason: string | null
}

export interface BlockPayload {
  dateFrom: string
  dateTo: string
  reason?: string
}

/** A selected rental period; `to` is null while only the first day is picked. */
export interface DateRange {
  from: string
  to: string | null
}
