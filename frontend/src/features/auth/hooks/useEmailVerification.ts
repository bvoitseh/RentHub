import { useMutation, useQueryClient } from '@tanstack/react-query'
import { authApi } from '@/features/auth/api/authApi'
import { currentUserKey } from '@/features/auth/hooks/useCurrentUser'

/** Consumes the link token; the cached user (if logged in) is refreshed so the banner disappears. */
export function useVerifyEmail() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: authApi.verifyEmail,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: currentUserKey }),
  })
}

export function useResendVerification() {
  return useMutation({ mutationFn: authApi.resendVerification })
}
