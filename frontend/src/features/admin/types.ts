import type { OwnerType } from '@/features/auth/types'
import type { EquipmentStatus } from '@/features/catalog/types'

export interface AdminUser {
  id: number
  name: string
  email: string
  phone: string | null
  city: string | null
  ownerType: OwnerType | null
  emailVerified: boolean
  blocked: boolean
  deactivated: boolean
  complaintCount: number
  createdAt: string
}

export interface AdminEquipment {
  id: number
  title: string
  categoryName: string
  pricePerDay: number
  status: EquipmentStatus
  ownerId: number
  ownerName: string
  ownerBlocked: boolean
  createdAt: string
}

export interface ModerationResult {
  id: number
  blocked: boolean
  cancelledBookings: number
}
