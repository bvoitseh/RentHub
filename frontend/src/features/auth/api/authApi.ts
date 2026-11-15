import { api } from '@/api/client'
import type { Credentials, RegisterPayload, User } from '@/features/auth/types'

/** /api/v1/auth endpoints. */
export const authApi = {
  me: (signal?: AbortSignal) => api.get<User>('auth/me', undefined, signal),
  register: (payload: RegisterPayload) => api.post<User>('auth/register', payload),
  login: (credentials: Credentials) => api.post<User>('auth/login', credentials),
  restore: (credentials: Credentials) => api.post<User>('auth/restore', credentials),
  logout: () => api.post<void>('auth/logout'),
  verifyEmail: (token: string) => api.post<void>('auth/verify-email', { token }),
  resendVerification: () => api.post<void>('auth/resend-verification'),
  forgotPassword: (email: string) => api.post<void>('auth/forgot-password', { email }),
  resetPassword: (token: string, password: string) => api.post<void>('auth/reset-password', { token, password }),
}
