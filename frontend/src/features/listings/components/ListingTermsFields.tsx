import { useWatch, type UseFormReturn } from 'react-hook-form'
import { Checkbox } from '@/components/ui/Checkbox'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import type { ListingInput } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'

const t = uk.listings.fields

/** Price, units, min/max days, pickup/delivery and the delivery note (FR-4b, FR-4c). */
export function ListingTermsFields({ form }: { form: UseFormReturn<ListingInput> }) {
  const { errors } = form.formState
  const delivery = useWatch({ control: form.control, name: 'deliveryAvailable' })

  const number = (name: 'pricePerDay' | 'quantity' | 'minDays' | 'maxDays', label: string, hint?: string) => (
    <FormField label={label} htmlFor={`l-${name}`} error={errors[name]?.message} hint={hint}>
      <Input
        id={`l-${name}`}
        inputMode={name === 'pricePerDay' ? 'decimal' : 'numeric'}
        invalid={!!errors[name]}
        {...form.register(name)}
      />
    </FormField>
  )

  return (
    <div className="flex flex-col gap-1">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        {number('pricePerDay', t.pricePerDay)}
        {number('quantity', t.quantity)}
        {number('minDays', t.minDays)}
        {number('maxDays', t.maxDays, t.maxDaysHint)}
      </div>
      <div className="flex flex-wrap gap-6">
        <Checkbox id="l-pickup" label={t.pickup} {...form.register('pickupAvailable')} />
        <Checkbox id="l-delivery" label={t.delivery} {...form.register('deliveryAvailable')} />
      </div>
      <p className="min-h-4 text-xs text-red-600" aria-live="polite">
        {errors.deliveryAvailable?.message}
      </p>
      {delivery && (
        <FormField label={t.deliveryNote} htmlFor="l-deliveryNote" error={errors.deliveryNote?.message} hint={t.deliveryNoteHint}>
          <Input id="l-deliveryNote" maxLength={500} {...form.register('deliveryNote')} />
        </FormField>
      )}
    </div>
  )
}
