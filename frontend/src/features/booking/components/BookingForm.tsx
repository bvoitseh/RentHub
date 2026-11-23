import { useState } from 'react'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { isProblem, problemMessage } from '@/api/problem'
import { PriceSummary } from '@/features/booking/components/PriceSummary'
import { daysBetween, formatDate } from '@/features/booking/dates'
import { useAvailability } from '@/features/booking/hooks/useBookingQueries'
import { useCreateBooking } from '@/features/booking/hooks/useBookingMutations'
import type { DateRange, Handover } from '@/features/booking/types'
import type { EquipmentDetails } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.booking.form

export interface BookingFormProps {
  item: EquipmentDetails
  range: DateRange | null
  units: number
  onUnitsChange: (units: number) => void
  onClear: () => void
  onSent: () => void
}

/** Steps 2–3 of a booking (NFR-6): units and handover, the instant price, and the request itself (FR-10). */
export function BookingForm({ item, range, units, onUnitsChange, onClear, onSent }: BookingFormProps) {
  const [handover, setHandover] = useState<Handover>(item.pickupAvailable ? 'PICKUP' : 'DELIVERY')
  const create = useCreateBooking()
  const to = range?.to ?? null
  const check = useAvailability(item.id, range?.from ?? '', to ?? '', Boolean(range && to))
  const days = range && to ? daysBetween(range.from, to) : 0
  const lengthError =
    days === 0 ? null : days < item.minDays ? t.tooShort(item.minDays) : item.maxDays && days > item.maxDays ? t.tooLong(item.maxDays) : null
  const lacking = (check.isPlaceholderData ? undefined : check.data)?.days.filter((d) => d.freeUnits < units).map((d) => d.date) ?? []
  const serverDays = isProblem(create.error) ? (create.error.problem.days as string[] | undefined) : undefined

  const submit = () => {
    if (!range || !to) return
    create.mutate({ equipmentId: item.id, units, dateFrom: range.from, dateTo: to, handover }, { onSuccess: onSent })
  }

  if (!range || !to) return <p className="text-sm text-slate-600">{uk.booking.calendar.pickHint}</p>

  return (
    <div className="flex flex-col gap-3">
      <FormField label={t.units} htmlFor="booking-units">
        <Input
          id="booking-units"
          type="number"
          min={1}
          max={item.quantity}
          value={units}
          onChange={(e) => onUnitsChange(Math.min(item.quantity, Math.max(1, Number(e.target.value) || 1)))}
        />
      </FormField>
      <fieldset className="flex flex-col gap-1">
        <legend className="mb-1 text-sm font-medium text-slate-800">{t.handover}</legend>
        {(['PICKUP', 'DELIVERY'] as const)
          .filter((h) => (h === 'PICKUP' ? item.pickupAvailable : item.deliveryAvailable))
          .map((h) => (
            <label key={h} className="flex items-center gap-2 text-sm text-slate-700">
              <input type="radio" name="handover" checked={handover === h} onChange={() => setHandover(h)} />
              {h === 'PICKUP' ? t.pickup : t.delivery}
            </label>
          ))}
      </fieldset>
      <PriceSummary from={range.from} to={to} days={days} units={units} pricePerDay={item.pricePerDay} />
      {handover === 'DELIVERY' && <p className="text-xs text-slate-500">{t.deliveryNote}</p>}
      {lengthError && <Alert tone="warning">{lengthError}</Alert>}
      {(lacking.length > 0 || serverDays) && (
        <Alert tone="error" title={t.lacking}>
          {(serverDays ?? lacking).map(formatDate).join(', ')}
        </Alert>
      )}
      {create.isError && !serverDays && <Alert tone="error">{problemMessage(create.error)}</Alert>}
      <div className="flex flex-wrap gap-2">
        <Button loading={create.isPending} disabled={Boolean(lengthError) || lacking.length > 0} onClick={submit}>
          {t.submit}
        </Button>
        <Button variant="secondary" onClick={onClear}>
          {t.clear}
        </Button>
      </div>
    </div>
  )
}
