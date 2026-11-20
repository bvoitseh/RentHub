import { CatalogFilterPanel } from '@/features/catalog/components/CatalogFilterPanel'
import { CatalogResults } from '@/features/catalog/components/CatalogResults'
import { CatalogSearchBar } from '@/features/catalog/components/CatalogSearchBar'
import { NearMeFilter } from '@/features/catalog/components/NearMeFilter'
import { SortSelect } from '@/features/catalog/components/SortSelect'
import { useCatalogFilters } from '@/features/catalog/hooks/useCatalogFilters'
import { uk } from '@/i18n/uk'

/** FR-6, FR-7: keyword search, filters and sorting kept in the URL, paged results. */
export function CatalogPage() {
  const { filters, update, reset } = useCatalogFilters()

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold text-slate-900">{uk.catalog.title}</h1>
      <CatalogSearchBar initial={filters.q} onSearch={(q) => update({ q })} />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[280px_1fr]">
        <aside className="flex flex-col gap-4">
          <NearMeFilter filters={filters} onChange={update} />
          <CatalogFilterPanel filters={filters} onApply={update} onReset={reset} />
        </aside>
        <CatalogResults
          filters={filters}
          onPage={(page) => update({ page: String(page) })}
          header={<SortSelect filters={filters} onChange={(sort) => update({ sort })} />}
        />
      </div>
    </div>
  )
}
