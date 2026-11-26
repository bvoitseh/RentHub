/** FR-21: a complaint relates to exactly one booking or one listing. */
export type ComplaintSubject = { bookingId: number } | { equipmentId: number }

export type ComplaintStatus = 'NEW' | 'RESOLVED' | 'REJECTED'

export interface Complaint {
  id: number
  author: { id: number; name: string }
  against: { id: number; name: string }
  bookingId: number | null
  equipment: { id: number; title: string } | null
  description: string
  status: ComplaintStatus
  createdAt: string
  decidedAt: string | null
}
