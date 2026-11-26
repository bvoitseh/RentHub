import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { ErrorState } from '@/components/ui/ErrorState'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { LoadingState } from '@/components/ui/Spinner'
import { StatTile } from '@/features/admin/components/StatTile'
import { formatNumber } from '@/features/admin/format'
import { useStatistics } from '@/features/admin/hooks/useAdminCatalog'
import { todayIso } from '@/features/booking/dates'
import { formatPrice } from '@/features/catalog/format'
import { uk } from '@/i18n/uk'

const t = uk.adminCatalog.statistics

/** FR-23 «Статистика»: E1-E5 for a chosen period, the current month by default. */
export function AdminStatistics() {
  const today = todayIso()
  const [period, setPeriod] = useState({ from: `${today.slice(0, 8)}01`, to: today })
  const [draft, setDraft] = useState(period)
  const { data, isPending, isError, error, refetch } = useStatistics(period.from, period.to)

  return (
    <div className="flex flex-col gap-4">
      <form
        className="flex flex-wrap items-start gap-2"
        onSubmit={(event) => {
          event.preventDefault()
          setPeriod(draft)
        }}
      >
        <FormField label={t.from} htmlFor="stats-from">
          <Input
            id="stats-from"
            type="date"
            value={draft.from}
            onChange={(e) => setDraft({ ...draft, from: e.target.value })}
          />
        </FormField>
        <FormField label={t.to} htmlFor="stats-to">
          <Input
            id="stats-to"
            type="date"
            value={draft.to}
            onChange={(e) => setDraft({ ...draft, to: e.target.value })}
          />
        </FormField>
        <Button type="submit" className="mt-6" disabled={!draft.from || !draft.to || draft.from > draft.to}>
          {t.show}
        </Button>
      </form>
      {isPending && <LoadingState />}
      {isError && <ErrorState error={error} onRetry={() => void refetch()} />}
      {data && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <StatTile label={t.e1} value={String(data.activeListings)} note={t.e1Note} />
          <StatTile
            label={t.e2}
            value={data.conflictFreeShare === null ? t.none : `${formatNumber(data.conflictFreeShare, 1)} %`}
            note={t.e2Note(data.checkedBookings)}
          />
          <StatTile
            label={t.e3}
            value={data.averageRating === null ? t.none : `★ ${formatNumber(data.averageRating)}`}
            note={t.e3Note(data.reviewCount)}
          />
          <StatTile label={t.e4} value={formatPrice(data.totalCommission)} />
          <StatTile label={t.e5} value={String(data.cities)} />
        </div>
      )}
    </div>
  )
}
