import { api } from '@/api/client'
import type { AdminEquipment, AdminUser, ModerationResult } from '@/features/admin/types'
import type { Page } from '@/features/catalog/types'
import type { Complaint, ComplaintStatus } from '@/features/complaints/types'

/** FR-20, FR-22: the admin's complaints, users and listings (ADMIN only on the server). */
export const adminApi = {
  complaints: (status: ComplaintStatus | '', page: number, signal?: AbortSignal) =>
    api.get<Page<Complaint>>('admin/complaints', { status, page }, signal),
  decide: (id: number, action: 'resolve' | 'reject') => api.post<Complaint>(`admin/complaints/${id}/${action}`),
  users: (q: string, page: number, signal?: AbortSignal) =>
    api.get<Page<AdminUser>>('admin/users', { q, page }, signal),
  equipment: (q: string, page: number, signal?: AbortSignal) =>
    api.get<Page<AdminEquipment>>('admin/equipment', { q, page }, signal),
  moderate: (kind: 'users' | 'equipment', id: number, action: 'block' | 'unblock') =>
    api.post<ModerationResult>(`admin/${kind}/${id}/${action}`),
}
