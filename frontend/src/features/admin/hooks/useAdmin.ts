import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '@/features/admin/api/adminApi'
import type { ComplaintStatus } from '@/features/complaints/types'

export function useAdminComplaints(status: ComplaintStatus | '', page: number) {
  return useQuery({
    queryKey: ['admin', 'complaints', status, page],
    queryFn: ({ signal }) => adminApi.complaints(status, page, signal),
    placeholderData: keepPreviousData,
  })
}

export function useAdminUsers(q: string, page: number) {
  return useQuery({
    queryKey: ['admin', 'users', q, page],
    queryFn: ({ signal }) => adminApi.users(q, page, signal),
    placeholderData: keepPreviousData,
  })
}

export function useAdminEquipment(q: string, page: number) {
  return useQuery({
    queryKey: ['admin', 'equipment', q, page],
    queryFn: ({ signal }) => adminApi.equipment(q, page, signal),
    placeholderData: keepPreviousData,
  })
}

/** Moderation changes lists, the catalogue and bookings, so all of them are refreshed. */
function useRefresh() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: ['admin'] })
    void queryClient.invalidateQueries({ queryKey: ['catalog'] })
    void queryClient.invalidateQueries({ queryKey: ['bookings'] })
  }
}

export function useDecideComplaint() {
  const onSuccess = useRefresh()
  return useMutation({
    mutationFn: ({ id, action }: { id: number; action: 'resolve' | 'reject' }) => adminApi.decide(id, action),
    onSuccess,
  })
}

export function useModerate(kind: 'users' | 'equipment') {
  const onSuccess = useRefresh()
  return useMutation({
    mutationFn: ({ id, action }: { id: number; action: 'block' | 'unblock' }) => adminApi.moderate(kind, id, action),
    onSuccess,
  })
}
