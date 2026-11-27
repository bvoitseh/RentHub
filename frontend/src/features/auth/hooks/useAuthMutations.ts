import { useMutation, useQueryClient } from '@tanstack/react-query'
import { authApi } from '@/features/auth/api/authApi'
import { clearSession, currentUserKey } from '@/features/auth/hooks/useCurrentUser'
import type { User } from '@/features/auth/types'

function useSetCurrentUser() {
  const queryClient = useQueryClient()
  return (user: User) => queryClient.setQueryData(currentUserKey, user)
}

export function useLogin() {
  const setUser = useSetCurrentUser()
  return useMutation({ mutationFn: authApi.login, onSuccess: setUser })
}

export function useRestoreAccount() {
  const setUser = useSetCurrentUser()
  return useMutation({ mutationFn: authApi.restore, onSuccess: setUser })
}

export function useRegister() {
  const setUser = useSetCurrentUser()
  return useMutation({ mutationFn: authApi.register, onSuccess: setUser })
}

/** Logs out and drops every cached query: nothing from the previous session may survive. */
export function useLogout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: authApi.logout,
    onSettled: () => clearSession(queryClient),
  })
}
