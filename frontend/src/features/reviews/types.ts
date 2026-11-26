export interface ReviewPerson {
  id: number
  name: string
  deactivated: boolean
}

/** FR-18: published reviews are public; an unpublished one is returned only to its author. */
export interface Review {
  id: number
  bookingId: number
  equipment: { id: number; title: string }
  author: ReviewPerson
  target: ReviewPerson
  rating: number
  comment: string
  createdAt: string
  published: boolean
  publishedAt: string | null
  reply: string | null
  repliedAt: string | null
  /** The viewer is the target and may still reply. */
  canReply: boolean
}

export interface BookingReviews {
  mine: Review | null
  theirs: Review | null
  canReview: boolean
  reviewUntil: string | null
}

export interface ReviewPayload {
  rating: number
  comment: string
}
