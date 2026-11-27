import type { Settlement } from '@/features/address/types'
import type { OwnerType } from '@/features/auth/types'

export type ItemCondition = 'NEW' | 'EXCELLENT' | 'GOOD' | 'FAIR'
export type EquipmentStatus = 'ACTIVE' | 'ARCHIVED' | 'BLOCKED'
export type CatalogSort = 'newest' | 'price,asc' | 'price,desc' | 'distance' | 'rating'

export interface Category {
  id: number
  name: string
  seasonFrom: number
  seasonTo: number
}

/** The owner as shown on cards; rating is null until the reviews feature. */
export interface OwnerBadge {
  id: number
  name: string
  city: string | null
  ownerType: OwnerType | null
  companyName: string | null
  rating: number | null
  reviewCount: number
}

/** FR-4a (D-52): the listing's optional video. */
export interface Video {
  url: string
  contentType: string
}

export interface Photo {
  id: number
  url: string
  position: number
}

export interface EquipmentSummary {
  id: number
  title: string
  pricePerDay: number
  categoryName: string
  city: string
  address: string
  coverUrl: string | null
  quantity: number
  minDays: number
  maxDays: number | null
  pickupAvailable: boolean
  deliveryAvailable: boolean
  status: EquipmentStatus
  owner: OwnerBadge
  distanceKm: number | null
  /** FR-7: days × units × price per day when the search had rental dates. */
  totalPrice: number | null
}

export interface EquipmentDetails {
  id: number
  title: string
  description: string
  category: Category
  condition: ItemCondition
  pricePerDay: number
  quantity: number
  minDays: number
  maxDays: number | null
  pickupAvailable: boolean
  deliveryAvailable: boolean
  deliveryNote: string | null
  address: string
  city: string
  settlement: Settlement
  postalCode: string | null
  lat: number
  lon: number
  status: EquipmentStatus
  photos: Photo[]
  video: Video | null
  owner: OwnerBadge
  mine: boolean
  createdAt: string
}

export interface Page<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}

/** FR-7 filters as they travel in the URL and the API query string (all optional). */
export interface CatalogFilters {
  q?: string
  categoryId?: string
  priceMin?: string
  priceMax?: string
  dateFrom?: string
  dateTo?: string
  units?: string
  lat?: string
  lon?: string
  radiusKm?: string
  pickup?: string
  delivery?: string
  ownerType?: OwnerType
  sort?: CatalogSort
  page?: string
}

export interface PublicProfile {
  id: number
  name: string
  city: string | null
  ownerType: OwnerType | null
  companyName: string | null
  deactivated: boolean
  rating: number | null
  reviewCount: number
  ownerCancellations: number
  complaintCount: number
  memberSince: string
}
