import { useQuery } from '@tanstack/react-query'
import { api } from '@/api/client'

export interface HealthResponse {
  status: 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN'
}

/** Backend liveness (Spring Boot Actuator), outside /api/v1 and public. */
export function useBackendHealth() {
  return useQuery({
    queryKey: ['health'],
    queryFn: ({ signal }) => api.get<HealthResponse>('/actuator/health', undefined, signal),
    staleTime: 60_000,
    retry: false,
  })
}
