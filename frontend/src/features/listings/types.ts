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
  settlementId: number
  address: string
  postalCode: string | null
  lat: number
  lon: number
}

/** FR-4 (D-48): the pin and suggested postal code; exact=false means the settlement centre (street not found). */
export interface ListingLocation {
  lat: number
  lon: number
  displayName: string
  postalCode: string | null
  exact: boolean
}
