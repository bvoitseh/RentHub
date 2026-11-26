import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { reviewsApi } from '@/features/reviews/api/reviewsApi'
import type { ReviewPayload } from '@/features/reviews/types'

export const reviewKeys = {
  all: ['reviews'] as const,
  booking: (id: number) => ['reviews', 'booking', id] as const,
  user: (id: number, page: number) => ['reviews', 'user', id, page] as const,
}

export function useBookingReviews(bookingId: number) {
  return useQuery({
    queryKey: reviewKeys.booking(bookingId),
    queryFn: ({ signal }) => reviewsApi.forBooking(bookingId, signal),
  })
}

export function useUserReviews(userId: number, page: number) {
  return useQuery({
    queryKey: reviewKeys.user(userId, page),
    queryFn: ({ signal }) => reviewsApi.forUser(userId, page, signal),
    placeholderData: keepPreviousData,
  })
}

/** A new review or reply can publish reviews and change ratings shown on profiles, cards and booking lists. */
function useRefresh() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: reviewKeys.all })
    void queryClient.invalidateQueries({ queryKey: ['catalog'] })
    void queryClient.invalidateQueries({ queryKey: ['bookings'] })
  }
}

export function useCreateReview(bookingId: number) {
  const onSuccess = useRefresh()
  return useMutation({ mutationFn: (payload: ReviewPayload) => reviewsApi.create(bookingId, payload), onSuccess })
}

export function useReply(reviewId: number) {
  const onSuccess = useRefresh()
  return useMutation({ mutationFn: (text: string) => reviewsApi.reply(reviewId, text), onSuccess })
}
