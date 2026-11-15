import { useQuery } from '@tanstack/react-query'
import { isProblem } from '@/api/problem'
import { authApi } from '@/features/auth/api/authApi'
import type { User } from '@/features/auth/types'

export const currentUserKey = ['auth', 'me'] as const

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
