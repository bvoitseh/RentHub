/** FR-22: a category with the number of its listings in any status. */
export interface AdminCategory {
  id: number
  name: string
  seasonFrom: number
  seasonTo: number
  listingCount: number
}

export interface CategoryPayload {
  name: string
  seasonFrom: number
  seasonTo: number
}

export interface Commission {
  rate: number
  updatedAt: string
  history: { rate: number; validFrom: string }[]
}

/** FR-23 E1-E5; null when there is nothing to measure in the period. */
export interface Statistics {
  from: string
  to: string
  activeListings: number
  conflictFreeShare: number | null
  checkedBookings: number
  averageRating: number | null
  reviewCount: number
  totalCommission: number
  cities: number
}
