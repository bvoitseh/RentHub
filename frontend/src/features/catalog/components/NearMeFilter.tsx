import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Select'
import { useBrowserLocation } from '@/features/catalog/hooks/useBrowserLocation'
import type { CatalogFilters } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

const t = uk.catalog.filters
const RADII = ['5', '10', '25', '50', '100']

export interface NearMeFilterProps {
  filters: CatalogFilters
  onChange: (changes: Partial<CatalogFilters>) => void
}

/** FR-7 distance: the browser position (asked on click only), an optional radius, nearest first. */
export function NearMeFilter({ filters, onChange }: NearMeFilterProps) {
  const { locate, status } = useBrowserLocation()
  const active = Boolean(filters.lat && filters.lon)

  const enable = async () => {
    const point = await locate()
    if (point) onChange({ lat: String(point.lat), lon: String(point.lon), sort: 'distance' })
  }

  const disable = () =>
    onChange({ lat: undefined, lon: undefined, radiusKm: undefined, sort: filters.sort === 'distance' ? undefined : filters.sort })

  return (
    <div className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-white p-3">
      <div className="flex flex-wrap items-center gap-2">
        <Button variant={active ? 'primary' : 'secondary'} loading={status === 'locating'} onClick={active ? disable : enable}>
          {active ? `✓ ${t.nearMe}` : t.nearMe}
        </Button>
        {active && (
          <Select
            aria-label={t.radius}
            className="w-auto"
            value={filters.radiusKm ?? ''}
            placeholder={t.radius}
            options={RADII.map((r) => ({ value: r, label: `${r} км` }))}
            onChange={(event) => onChange({ radiusKm: event.target.value || undefined })}
          />
        )}
      </div>
      <p className={`text-xs ${status === 'denied' ? 'text-red-600' : 'text-slate-500'}`}>
        {status === 'denied' ? t.locationDenied : status === 'locating' ? t.locating : t.locationHint}
      </p>
    </div>
  )
}
