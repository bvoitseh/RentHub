import { Select } from '@/components/ui/Select'
import type { CatalogFilters, CatalogSort } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

export interface SortSelectProps {
  filters: CatalogFilters
  onChange: (sort: CatalogSort | undefined) => void
}

/** FR-7 sorting; "nearest" needs a point, so it is offered only when "near me" is on. */
export function SortSelect({ filters, onChange }: SortSelectProps) {
  const t = uk.catalog.sort
  const options: { value: CatalogSort; label: string }[] = [
    { value: 'newest', label: t.newest },
    { value: 'price,asc', label: t.priceAsc },
    { value: 'price,desc', label: t.priceDesc },
    { value: 'rating', label: t.rating },
  ]
  if (filters.lat && filters.lon) options.splice(1, 0, { value: 'distance', label: t.distance })

  return (
    <label className="flex items-center gap-2 text-sm text-slate-600">
      {t.label}
      <Select
        className="w-auto"
        value={filters.sort ?? 'newest'}
        options={options}
        onChange={(event) => {
          const value = event.target.value as CatalogSort
          onChange(value === 'newest' ? undefined : value)
        }}
      />
    </label>
  )
}
