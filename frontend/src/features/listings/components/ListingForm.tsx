import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { applyServerErrors } from '@/api/formErrors'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import type { EquipmentDetails } from '@/features/catalog/types'
import { FormSection as Section } from '@/features/listings/components/FormSection'
import { ListingBasicsFields } from '@/features/listings/components/ListingBasicsFields'
import { ListingLocationFields } from '@/features/listings/components/ListingLocationFields'
import { ListingTermsFields } from '@/features/listings/components/ListingTermsFields'
import { useSaveListing } from '@/features/listings/hooks/useListings'
import { listingSchema, toListingPayload, type ListingInput } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'

const t = uk.listings

export interface ListingFormProps {
  initial: ListingInput
  listingId?: number
  onSaved: (item: EquipmentDetails) => void
}

/** Create or edit a listing (FR-4, FR-5); photos are managed separately once the listing exists. */
export function ListingForm({ initial, listingId, onSaved }: ListingFormProps) {
  const save = useSaveListing(listingId)
  const form = useForm<ListingInput>({ resolver: zodResolver(listingSchema), defaultValues: initial })

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const item = await save.mutateAsync(toListingPayload(values))
      form.reset(values)
      onSaved(item)
    } catch (error) {
      applyServerErrors(error, form.setError)
    }
  })

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-4">
      {save.isError && <Alert tone="error">{problemMessage(save.error)}</Alert>}
      {save.isSuccess && listingId && !form.formState.isDirty && <Alert tone="success">{t.saved}</Alert>}
      <Section title={t.sections.basics}>
        <ListingBasicsFields form={form} />
      </Section>
      <Section title={t.sections.terms}>
        <ListingTermsFields form={form} />
      </Section>
      <Section title={t.sections.location}>
        <ListingLocationFields form={form} />
      </Section>
      <div className="flex justify-end">
        <Button type="submit" loading={save.isPending}>
          {listingId ? t.save : t.saveAndPhotos}
        </Button>
      </div>
    </form>
  )
}
