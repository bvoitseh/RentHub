import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Checkbox } from '@/components/ui/Checkbox'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { useAutoApply } from '@/features/catalog/hooks/useAutoApply'
import { useCategories } from '@/features/catalog/hooks/useCatalogQueries'
import type { CatalogFilters } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog.filters

export interface CatalogFilterPanelProps {
  filters: CatalogFilters
  onApply: (changes: Partial<CatalogFilters>) => void
  onReset: () => void
}

const FIELDS = ['categoryId', 'priceMin', 'priceMax', 'dateFrom', 'dateTo', 'units', 'ownerType'] as const

/**
 * FR-7 filters, applied automatically as they change. Half-filled pairs (one date only, min price above max) keep the
 * previous values until they are valid. The form is uncontrolled and re-created only on reset, so typing keeps focus.
 */
export function CatalogFilterPanel({ filters, onApply, onReset }: CatalogFilterPanelProps) {
  const { data: categories = [] } = useCategories()
  const today = new Date().toLocaleDateString('sv-SE') // local YYYY-MM-DD

  const submit = (form: HTMLFormElement) => {
    const data = new FormData(form)
    const changes: Partial<CatalogFilters> = {}
    for (const name of FIELDS) Object.assign(changes, { [name]: String(data.get(name) ?? '') || undefined })
    changes.pickup = data.get('pickup') ? 'true' : undefined
    changes.delivery = data.get('delivery') ? 'true' : undefined
    if (Boolean(changes.dateFrom) !== Boolean(changes.dateTo)) {
      delete changes.dateFrom
      delete changes.dateTo
    }
    if (changes.priceMin && changes.priceMax && Number(changes.priceMin) > Number(changes.priceMax)) {
      delete changes.priceMin
      delete changes.priceMax
    }
    onApply(changes)
  }
  const onChange = useAutoApply(submit)
  const [version, setVersion] = useState(0)

  const number = (name: 'priceMin' | 'priceMax' | 'units', label: string, min: number) => (
    <FormField label={label} htmlFor={`f-${name}`}>
      <Input id={`f-${name}`} name={name} type="number" min={min} inputMode="numeric" defaultValue={filters[name]} />
    </FormField>
  )

  return (
    <form
      key={version}
      className="flex flex-col gap-1"
      onChange={onChange}
      onSubmit={(event) => {
        event.preventDefault()
        submit(event.currentTarget)
      }}
    >
      <h2 className="mb-2 text-base font-semibold text-slate-900">{t.title}</h2>
      <FormField label={t.category} htmlFor="f-categoryId">
        <Select
          id="f-categoryId"
          name="categoryId"
          defaultValue={filters.categoryId ?? ''}
          placeholder={uk.catalog.allCategories}
          options={categories.map((c) => ({ value: String(c.id), label: c.name }))}
        />
      </FormField>
      <div className="grid grid-cols-2 gap-2">
        {number('priceMin', t.priceMin, 0)}
        {number('priceMax', t.priceMax, 0)}
      </div>
      <div className="grid grid-cols-2 gap-2">
        <FormField label={t.dateFrom} htmlFor="f-dateFrom">
          <Input id="f-dateFrom" name="dateFrom" type="date" min={today} defaultValue={filters.dateFrom} />
        </FormField>
        <FormField label={t.dateTo} htmlFor="f-dateTo">
          <Input id="f-dateTo" name="dateTo" type="date" min={today} defaultValue={filters.dateTo} />
        </FormField>
      </div>
      {number('units', t.units, 1)}
      <FormField label={t.ownerType} htmlFor="f-ownerType">
        <Select
          id="f-ownerType"
          name="ownerType"
          defaultValue={filters.ownerType ?? ''}
          placeholder={t.anyOwner}
          options={[
            { value: 'PRIVATE', label: uk.catalog.ownerTypes.PRIVATE },
            { value: 'COMPANY', label: uk.catalog.ownerTypes.COMPANY },
          ]}
        />
      </FormField>
      <div className="mb-4 flex flex-col gap-2">
        <Checkbox id="f-pickup" name="pickup" label={t.pickup} defaultChecked={filters.pickup === 'true'} />
        <Checkbox id="f-delivery" name="delivery" label={t.delivery} defaultChecked={filters.delivery === 'true'} />
      </div>
      <Button
        variant="secondary"
        onClick={() => {
          onReset()
          setVersion((v) => v + 1)
        }}
      >
        {t.reset}
      </Button>
    </form>
  )
}
