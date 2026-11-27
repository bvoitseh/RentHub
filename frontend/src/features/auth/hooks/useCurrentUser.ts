import { hashKey, useQuery, type QueryClient } from '@tanstack/react-query'
import { isProblem } from '@/api/problem'
import { authApi } from '@/features/auth/api/authApi'
import type { User } from '@/features/auth/types'

export const currentUserKey = ['auth', 'me'] as const

/**
 * Ends the session in the cache (logout, deactivation): the current user becomes null in place, so mounted
 * components such as the header are notified, and every other cached query is dropped. `clear()` is not used: it
 * would also detach the header from the user query until something re-rendered it.
 */
export function clearSession(queryClient: QueryClient) {
  queryClient.setQueryData(currentUserKey, null)
  const userHash = hashKey(currentUserKey)
  queryClient.removeQueries({ predicate: (query) => query.queryHash !== userHash })
}

/** The logged-in account, or null for guests (a 401 is a normal answer, not an error). */
export function useCurrentUser() {
  return useQuery<User | null>({
    queryKey: currentUserKey,
    queryFn: async ({ signal }) => {
      try {
        return await authApi.me(signal)
      } catch (error) {
        if (isProblem(error) && error.status === 401) return null
        throw error
      }
    },
    staleTime: 5 * 60_000,
    retry: false,
  })
}
