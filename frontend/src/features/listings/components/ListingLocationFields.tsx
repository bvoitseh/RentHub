import { useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Controller, type UseFormReturn } from 'react-hook-form'
import { PinPicker } from '@/components/map/PinPicker'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { SettlementPicker } from '@/features/address/components/SettlementPicker'
import { listingsApi } from '@/features/listings/api/listingsApi'
import type { ListingInput } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'

const t = uk.listings

/**
 * FR-4 (D-48): region + settlement from our list, street and house, optional postal code. «Знайти на мапі» asks the
 * backend for the pin (street search within the settlement, else its centre) and fills an empty postal code.
 */
export function ListingLocationFields({ form }: { form: UseFormReturn<ListingInput> }) {
  const { errors } = form.formState
  const suggested = useRef<string | null>(null)
  const locate = useMutation({
    mutationFn: ({ settlementId, street }: { settlementId: number; street: string }) =>
      listingsApi.locate(settlementId, street),
    onSuccess: (result) => {
      form.setValue('point', { lat: result.lat, lon: result.lon }, { shouldDirty: true, shouldValidate: true })
      // Replace an empty field or our own earlier suggestion, never a code the owner typed (D-48).
      const current = form.getValues('postalCode')
      if (result.postalCode && (!current || current === suggested.current)) {
        form.setValue('postalCode', result.postalCode, { shouldDirty: true, shouldValidate: true })
        suggested.current = result.postalCode
      }
    },
  })

  const status = locate.isPending
    ? t.location.finding
    : locate.isSuccess
      ? locate.data.exact
        ? t.location.found
        : t.location.approximate
      : locate.isError
        ? t.location.notFound
        : t.location.hint

  return (
    <div className="flex flex-col gap-2">
      <Controller
        control={form.control}
        name="settlement"
        render={({ field: { value, onChange } }) => (
          <SettlementPicker idPrefix="l" value={value} onChange={onChange} error={errors.settlement?.message} />
        )}
      />
      <FormField label={t.fields.address} htmlFor="l-address" error={errors.address?.message}>
        <div className="flex gap-2">
          <Input id="l-address" maxLength={300} invalid={!!errors.address} {...form.register('address')} />
          <Button
            variant="secondary"
            loading={locate.isPending}
            onClick={() => {
              const settlement = form.getValues('settlement')
              if (!settlement) void form.trigger('settlement')
              else locate.mutate({ settlementId: settlement.id, street: form.getValues('address').trim() })
            }}
          >
            {t.location.find}
          </Button>
        </div>
      </FormField>
      <FormField
        label={t.fields.postalCode}
        htmlFor="l-postal"
        hint={t.fields.postalCodeHint}
        error={errors.postalCode?.message}
      >
        <Input
          id="l-postal"
          inputMode="numeric"
          maxLength={5}
          className="max-w-32"
          invalid={!!errors.postalCode}
          {...form.register('postalCode')}
        />
      </FormField>
      <p
        className={`text-sm ${locate.isError || locate.data?.exact === false ? 'text-amber-700' : 'text-slate-600'}`}
        aria-live="polite"
      >
        {status}
      </p>
      <Controller
        control={form.control}
        name="point"
        render={({ field }) => (
          <PinPicker value={field.value} onChange={(point) => field.onChange(point)} label={uk.catalog.map.pin} />
        )}
      />
      <p className="min-h-4 text-xs text-red-600" aria-live="polite">
        {errors.point?.message}
      </p>
    </div>
  )
}
