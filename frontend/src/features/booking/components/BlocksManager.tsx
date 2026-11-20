import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { LoadingState } from '@/components/ui/Spinner'
import { AvailabilityCalendar } from '@/features/booking/components/AvailabilityCalendar'
import { formatPeriod, todayIso } from '@/features/booking/dates'
import { useBlockMutations } from '@/features/booking/hooks/useBookingMutations'
import { useBlocks } from '@/features/booking/hooks/useBookingQueries'
import { uk } from '@/i18n/uk'

const t = uk.booking.blocks

/** FR-8: the owner closes and reopens date ranges of a listing; the calendar shows the result. */
export function BlocksManager({ equipmentId }: { equipmentId: number }) {
  const { data, isPending } = useBlocks(equipmentId)
  const { add, remove } = useBlockMutations(equipmentId)
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [reason, setReason] = useState('')
  const today = todayIso()

  const submit = () =>
    add.mutate(
      { dateFrom: from, dateTo: to || from, reason: reason.trim() || undefined },
      { onSuccess: () => (setFrom(''), setTo(''), setReason('')) },
    )

  return (
    <div className="flex flex-col gap-4">
      <p className="text-sm text-slate-600">{t.hint}</p>
      <AvailabilityCalendar equipmentId={equipmentId} units={1} />
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_1fr_2fr_auto] sm:items-end">
        <FormField label={t.from} htmlFor="block-from">
          <Input id="block-from" type="date" min={today} value={from} onChange={(e) => setFrom(e.target.value)} />
        </FormField>
        <FormField label={t.to} htmlFor="block-to">
          <Input id="block-to" type="date" min={from || today} value={to} onChange={(e) => setTo(e.target.value)} />
        </FormField>
        <FormField label={t.reason} htmlFor="block-reason">
          <Input id="block-reason" maxLength={200} value={reason} onChange={(e) => setReason(e.target.value)} />
        </FormField>
        <Button loading={add.isPending} disabled={!from} onClick={submit}>
          {t.add}
        </Button>
      </div>
      {add.isError && <Alert tone="error">{problemMessage(add.error)}</Alert>}
      {remove.isError && <Alert tone="error">{problemMessage(remove.error)}</Alert>}
      {isPending ? (
        <LoadingState />
      ) : !data || data.length === 0 ? (
        <p className="text-sm text-slate-500">{t.empty}</p>
      ) : (
        <ul className="flex flex-col divide-y divide-slate-100 rounded-md border border-slate-200">
          {data.map((b) => (
            <li key={b.id} className="flex items-center justify-between gap-3 px-3 py-2 text-sm">
              <span className="text-slate-800">
                {formatPeriod(b.dateFrom, b.dateTo)}
                {b.reason && <span className="text-slate-500"> · {b.reason}</span>}
              </span>
              <Button variant="ghost" loading={remove.isPending && remove.variables === b.id} onClick={() => remove.mutate(b.id)}>
                {t.remove}
              </Button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
