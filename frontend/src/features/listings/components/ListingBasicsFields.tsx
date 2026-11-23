import { useEffect } from 'react'
import type { UseFormReturn } from 'react-hook-form'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { useCategories } from '@/features/catalog/hooks/useCatalogQueries'
import type { ListingInput } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'

const t = uk.listings.fields
const textarea =
  'block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-xs focus:outline-2 focus:outline-brand-600'

/** Title, description, category, condition (FR-4). */
export function ListingBasicsFields({ form }: { form: UseFormReturn<ListingInput> }) {
  const { data: categories = [] } = useCategories()
  const { getValues, setValue } = form

  // The select is registered before the categories load, when only "—" exists, so the browser shows "—" although the
  // form holds the listing's category. Writing the same value again once the options exist selects it on screen.
  useEffect(() => {
    if (categories.length > 0) setValue('categoryId', getValues('categoryId'))
  }, [categories.length, getValues, setValue])
  const { errors } = form.formState
  const conditions = Object.entries(uk.catalog.conditions).map(([value, label]) => ({ value, label }))

  return (
    <div className="flex flex-col gap-1">
      <FormField label={t.title} htmlFor="l-title" error={errors.title?.message}>
        <Input id="l-title" maxLength={120} invalid={!!errors.title} {...form.register('title')} />
      </FormField>
      <FormField label={t.description} htmlFor="l-description" error={errors.description?.message}>
        <textarea
          id="l-description"
          rows={5}
          maxLength={4000}
          aria-invalid={!!errors.description || undefined}
          className={textarea}
          {...form.register('description')}
        />
      </FormField>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <FormField label={t.category} htmlFor="l-category" error={errors.categoryId?.message}>
          <Select
            id="l-category"
            placeholder="—"
            invalid={!!errors.categoryId}
            options={categories.map((c) => ({ value: String(c.id), label: c.name }))}
            {...form.register('categoryId')}
          />
        </FormField>
        <FormField label={t.condition} htmlFor="l-condition" error={errors.condition?.message}>
          <Select id="l-condition" options={conditions} {...form.register('condition')} />
        </FormField>
      </div>
    </div>
  )
}
