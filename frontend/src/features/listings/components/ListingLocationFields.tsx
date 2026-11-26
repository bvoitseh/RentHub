import { useMutation } from '@tanstack/react-query'
import { Controller, type UseFormReturn } from 'react-hook-form'
import { PinPicker } from '@/components/map/PinPicker'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { listingsApi } from '@/features/listings/api/listingsApi'
import type { ListingInput } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'

const t = uk.listings

/** FR-4: the backend geocodes the address; the owner confirms or corrects the pin, or places it by hand. */
export function ListingLocationFields({ form }: { form: UseFormReturn<ListingInput> }) {
  const { errors } = form.formState
  const geocode = useMutation({
    mutationFn: (address: string) => listingsApi.geocode(address),
    onSuccess: (result) => {
      form.setValue('point', { lat: result.lat, lon: result.lon }, { shouldDirty: true, shouldValidate: true })
      if (result.city) form.setValue('city', result.city, { shouldDirty: true, shouldValidate: true })
    },
  })

  const status = geocode.isPending
    ? t.location.finding
    : geocode.isSuccess
      ? t.location.found
      : geocode.isError
        ? t.location.notFound
        : t.location.hint

  return (
    <div className="flex flex-col gap-2">
      <FormField label={t.fields.address} htmlFor="l-address" error={errors.address?.message}>
        <div className="flex gap-2">
          <Input id="l-address" maxLength={300} invalid={!!errors.address} {...form.register('address')} />
          <Button
            variant="secondary"
            loading={geocode.isPending}
            onClick={() => {
              const address = form.getValues('address').trim()
              if (address) geocode.mutate(address)
              else void form.trigger('address')
            }}
          >
            {t.location.find}
          </Button>
        </div>
      </FormField>
      <FormField label={t.fields.city} htmlFor="l-city" hint={t.fields.cityHint} error={errors.city?.message}>
        <Input id="l-city" maxLength={100} invalid={!!errors.city} {...form.register('city')} />
      </FormField>
      <p className={`text-sm ${geocode.isError ? 'text-amber-700' : 'text-slate-600'}`} aria-live="polite">
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
