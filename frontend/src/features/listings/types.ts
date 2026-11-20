import type { ItemCondition } from '@/features/catalog/types'

/** Body of POST/PUT /equipment (FR-4, FR-4b, FR-4c); lat/lon is the confirmed pin. */
export interface ListingPayload {
  categoryId: number
  title: string
  description: string
  condition: ItemCondition
  pricePerDay: number
  quantity: number
  minDays: number
  maxDays: number | null
  pickupAvailable: boolean
  deliveryAvailable: boolean
  deliveryNote: string | null
  address: string
  lat: number
  lon: number
}

export interface GeocodeResult {
  lat: number
  lon: number
  displayName: string
}
