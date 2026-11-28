import { api } from '@/api/client'
import type { ProfilePayload, User } from '@/features/auth/types'

/** /api/v1/users/me endpoints. */
export const accountApi = {
  updateProfile: (payload: ProfilePayload) => api.put<User>('users/me', payload),
  setGeoConsent: (consent: boolean) => api.put<User>('users/me/geo-consent', { consent }),
  setOnlineStatus: (visible: boolean) => api.put<User>('users/me/online-status', { visible }),
  deactivate: (password: string) => api.post<void>('users/me/deactivate', { password }),
}
