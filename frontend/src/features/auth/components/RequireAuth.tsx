import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import type { Role } from '@/features/auth/types'

export interface RequireAuthProps {
  roles?: Role[]
  children: ReactNode
}

/** Route guard: guests go to /login (and come back after), wrong roles get a 404-like redirect. */
export function RequireAuth({ roles, children }: RequireAuthProps) {
  const location = useLocation()
  const { data: user, isPending, isError, error, refetch } = useCurrentUser()

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />
  return children
}
