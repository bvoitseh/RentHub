import { api } from '@/api/client'
import type { Complaint, ComplaintSubject } from '@/features/complaints/types'

export const complaintsApi = {
  create: (subject: ComplaintSubject, description: string) =>
    api.post<Complaint>('complaints', { ...subject, description }),
}
