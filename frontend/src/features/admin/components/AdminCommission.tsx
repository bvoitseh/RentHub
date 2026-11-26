import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { ErrorState } from '@/components/ui/ErrorState'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { LoadingState } from '@/components/ui/Spinner'
import { useChangeCommission, useCommission } from '@/features/admin/hooks/useAdminCatalog'
import { formatNumber } from '@/features/admin/format'
import { formatDateTime } from '@/features/booking/dates'
import { uk } from '@/i18n/uk'

const t = uk.adminCatalog.commission
const RATE = /^\d{1,3}([.,]\d{1,2})?$/

/** FR-22 «Комісія»: the current rate, when it was set, a new rate (in force at once) and the history (D-46). */
export function AdminCommission() {
  const { data, isPending, isError, error, refetch } = useCommission()
  const change = useChangeCommission()
  const [rate, setRate] = useState('')
  const value = Number(rate.replace(',', '.'))
  const valid = RATE.test(rate) && value >= 0 && value <= 100

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />

  return (
    <div className="flex max-w-xl flex-col gap-4">
      <section className="rounded-lg border border-slate-200 bg-white p-4">
        <p className="text-sm text-slate-500">{t.current}</p>
        <p className="text-3xl font-bold text-slate-900">{formatNumber(data.rate)} %</p>
        <p className="text-sm text-slate-600">{t.since(formatDateTime(data.updatedAt))}</p>
      </section>
      <p className="text-sm text-slate-600">{t.note}</p>
      <form
        className="flex items-start gap-2"
        onSubmit={(event) => {
          event.preventDefault()
          if (valid) change.mutate(value, { onSuccess: () => setRate('') })
        }}
      >
        <FormField
          label={t.newRate}
          htmlFor="commission-rate"
          hint={t.rateHint}
          error={rate && !valid ? t.rateInvalid : undefined}
        >
          <Input id="commission-rate" inputMode="decimal" value={rate} onChange={(e) => setRate(e.target.value)} />
        </FormField>
        <Button type="submit" className="mt-6" loading={change.isPending} disabled={!valid}>
          {t.save}
        </Button>
      </form>
      {change.isSuccess && <Alert tone="success">{t.saved}</Alert>}
      {change.isError && <Alert tone="error">{problemMessage(change.error)}</Alert>}
      <section className="flex flex-col gap-2">
        <h2 className="text-sm font-medium text-slate-700">{t.history}</h2>
        <ul className="flex flex-col gap-1 text-sm text-slate-600">
          {data.history.map((h) => (
            <li key={h.validFrom}>
              {formatNumber(h.rate)} % — {formatDateTime(h.validFrom)}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
