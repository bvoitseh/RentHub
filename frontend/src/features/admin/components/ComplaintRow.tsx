import { Link } from 'react-router'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { useDecideComplaint } from '@/features/admin/hooks/useAdmin'
import { formatDateTime } from '@/features/booking/dates'
import type { Complaint } from '@/features/complaints/types'
import { uk } from '@/i18n/uk'

const t = uk.admin

/** FR-22: one complaint — who, against whom, what it relates to, the text and the decision buttons. */
export function ComplaintRow({ complaint: c }: { complaint: Complaint }) {
  const decide = useDecideComplaint()
  return (
    <li className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-surface p-3 text-sm sm:flex-row">
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <p className="flex flex-wrap items-center gap-2 text-slate-500">
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">
            {t.complaintStatuses[c.status]}
          </span>
          {formatDateTime(c.createdAt)} · №{c.id}
        </p>
        <p className="text-slate-700">
          {t.from}:{' '}
          <Link to={`/users/${c.author.id}`} className="text-brand-700 hover:underline">
            {c.author.name}
          </Link>
          {' · '}
          {t.against}:{' '}
          <Link to={`/users/${c.against.id}`} className="text-brand-700 hover:underline">
            {c.against.name}
          </Link>
        </p>
        <p className="text-slate-700">
          {c.bookingId !== null ? t.aboutBooking(c.bookingId) : t.aboutListing}
          {c.equipment && (
            <>
              {': '}
              <Link to={`/equipment/${c.equipment.id}`} className="text-brand-700 hover:underline">
                {c.equipment.title}
              </Link>
            </>
          )}
        </p>
        <p className="whitespace-pre-line text-slate-900">{c.description}</p>
        {decide.isError && <Alert tone="error">{problemMessage(decide.error)}</Alert>}
      </div>
      {c.status === 'NEW' && (
        <div className="flex shrink-0 gap-2 sm:flex-col">
          <Button loading={decide.isPending} onClick={() => decide.mutate({ id: c.id, action: 'resolve' })}>
            {t.resolve}
          </Button>
          <Button
            variant="secondary"
            disabled={decide.isPending}
            onClick={() => decide.mutate({ id: c.id, action: 'reject' })}
          >
            {t.reject}
          </Button>
        </div>
      )}
    </li>
  )
}
