import { QueryClient } from '@tanstack/react-query'
import { isProblem } from '@/api/problem'

/** Shared TanStack Query client: no retries on client errors (4xx), short default staleness. */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        if (isProblem(error) && error.status < 500) return false
        return failureCount < 2
      },
    },
    mutations: { retry: false },
  },
})
