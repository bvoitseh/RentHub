import type { Settlement } from '@/features/address/types'

/** USER can both rent and rent out equipment; ADMIN is the platform administrator. */
export type Role = 'USER' | 'ADMIN'
export type OwnerType = 'PRIVATE' | 'COMPANY'

/** The caller's own account as returned by GET /auth/me. */
export interface User {
  id: number
  name: string
  email: string
  emailVerified: boolean
  phone: string | null
  role: Role
  city: string | null
  /** Region + settlement (D-48); null only for the admin. */
  settlement: Settlement | null
  geoConsent: boolean
  /** D-58: chat partners see when the user is online. */
  showOnlineStatus: boolean
  /** Account type; null only for the admin. */
  ownerType: OwnerType | null
  companyName: string | null
  edrpou: string | null
}

export interface Credentials {
  email: string
  password: string
}

export interface RegisterPayload {
  name: string
  email: string
  phone: string
  password: string
  settlementId: number
  ownerType: OwnerType
  companyName?: string
  edrpou?: string
}

export interface ProfilePayload {
  name: string
  phone: string
  settlementId: number
  ownerType?: OwnerType
  companyName?: string
  edrpou?: string
}
