export type Role = 'RENTER' | 'OWNER' | 'ADMIN'
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
  geoConsent: boolean
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
  city: string
  role: Exclude<Role, 'ADMIN'>
  ownerType?: OwnerType
  companyName?: string
  edrpou?: string
}

export interface ProfilePayload {
  name: string
  phone: string
  city: string
  ownerType?: OwnerType
  companyName?: string
  edrpou?: string
}
