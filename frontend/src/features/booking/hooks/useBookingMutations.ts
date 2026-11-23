import { useMutation, useQueryClient } from '@tanstack/react-query'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { bookingKeys } from '@/features/booking/hooks/useBookingQueries'
import type { BlockPayload } from '@/features/booking/types'

/** Any booking change can alter lists, availability and catalogue date searches, so all of them are refetched. */
function useRefresh() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: bookingKeys.all })
    void queryClient.invalidateQueries({ queryKey: ['catalog', 'search'] })
  }
}

export function useCreateBooking() {
  const onSuccess = useRefresh()
  return useMutation({ mutationFn: bookingApi.create, onSuccess })
}

/** Confirm, reject and cancel (with an optional reason) for one booking. */
export function useBookingDecisions() {
  const onSuccess = useRefresh()
  return {
    confirm: useMutation({ mutationFn: bookingApi.confirm, onSuccess }),
    reject: useMutation({ mutationFn: bookingApi.reject, onSuccess }),
    cancel: useMutation({
      mutationFn: ({ id, reason }: { id: number; reason?: string }) => bookingApi.cancel(id, reason),
      onSuccess,
    }),
  }
}

export function useBlockMutations(equipmentId: number) {
  const onSuccess = useRefresh()
  return {
    add: useMutation({ mutationFn: (payload: BlockPayload) => bookingApi.addBlock(equipmentId, payload), onSuccess }),
    remove: useMutation({ mutationFn: (blockId: number) => bookingApi.removeBlock(equipmentId, blockId), onSuccess }),
  }
}
