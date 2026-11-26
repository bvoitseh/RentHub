import { useState } from 'react'
import { Select } from '@/components/ui/Select'
import { AdminList } from '@/features/admin/components/AdminList'
import { ComplaintRow } from '@/features/admin/components/ComplaintRow'
import { useAdminComplaints } from '@/features/admin/hooks/useAdmin'
import type { ComplaintStatus } from '@/features/complaints/types'
import { uk } from '@/i18n/uk'

const t = uk.admin
const statuses: ComplaintStatus[] = ['NEW', 'RESOLVED', 'REJECTED']

/** FR-22 «Скарги»: new ones first by default, any status on demand. */
export function AdminComplaints() {
  const [status, setStatus] = useState<ComplaintStatus | ''>('NEW')
  const [page, setPage] = useState(0)
  const query = useAdminComplaints(status, page)

  return (
    <div className="flex flex-col gap-4">
      <label className="flex w-56 flex-col gap-1 text-sm text-slate-700">
        {t.status}
        <Select
          value={status}
          options={[
            { value: '', label: t.all },
            ...statuses.map((s) => ({
              value: s,
              label: t.complaintStatuses[s],
            })),
          ]}
          onChange={(e) => {
            setStatus(e.target.value as ComplaintStatus | '')
            setPage(0)
          }}
        />
      </label>
      <AdminList
        query={query}
        empty={t.noComplaints}
        page={page}
        onPage={setPage}
        row={(c) => <ComplaintRow key={c.id} complaint={c} />}
      />
    </div>
  )
}
