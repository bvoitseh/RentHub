import { useMutation } from '@tanstack/react-query'
import { authApi } from '@/features/auth/api/authApi'

export function useForgotPassword() {
  return useMutation({ mutationFn: authApi.forgotPassword })
}

export function useResetPassword() {
  return useMutation({
    mutationFn: ({ token, password }: { token: string; password: string }) => authApi.resetPassword(token, password),
  })
}
