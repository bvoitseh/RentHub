import { useMutation, useQueryClient } from '@tanstack/react-query'
import { accountApi } from '@/features/auth/api/accountApi'
import { clearSession, currentUserKey } from '@/features/auth/hooks/useCurrentUser'
import type { User } from '@/features/auth/types'

function useSetCurrentUser() {
  const queryClient = useQueryClient()
  return (user: User) => queryClient.setQueryData(currentUserKey, user)
}

export function useUpdateProfile() {
  const setUser = useSetCurrentUser()
  return useMutation({ mutationFn: accountApi.updateProfile, onSuccess: setUser })
}

export function useSetGeoConsent() {
  const setUser = useSetCurrentUser()
  return useMutation({ mutationFn: accountApi.setGeoConsent, onSuccess: setUser })
}

/** After deactivation the backend has ended the session; `onDeactivated` redirects and the cache is wiped like on logout. */
export function useDeactivateAccount(onDeactivated: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: accountApi.deactivate,
    onSuccess: () => {
      onDeactivated()
      clearSession(queryClient)
    },
  })
}
