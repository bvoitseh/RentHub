import { api } from '@/api/client'
import type { Page } from '@/features/catalog/types'
import type { BookingReviews, Review, ReviewPayload } from '@/features/reviews/types'

/** Reviews of a booking (its sides), a user's published reviews, and replies (FR-18, FR-19). */
export const reviewsApi = {
  forBooking: (bookingId: number, signal?: AbortSignal) =>
    api.get<BookingReviews>(`bookings/${bookingId}/reviews`, undefined, signal),
  create: (bookingId: number, payload: ReviewPayload) => api.post<Review>(`bookings/${bookingId}/reviews`, payload),
  forUser: (userId: number, page: number, signal?: AbortSignal) =>
    api.get<Page<Review>>(`users/${userId}/reviews`, { page }, signal),
  reply: (reviewId: number, text: string) => api.post<Review>(`reviews/${reviewId}/reply`, { text }),
}
