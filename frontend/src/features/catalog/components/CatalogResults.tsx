import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { EquipmentCard } from '@/features/catalog/components/EquipmentCard'
import { Pagination } from '@/features/catalog/components/Pagination'
import { useCatalogSearch } from '@/features/catalog/hooks/useCatalogQueries'
import type { CatalogFilters } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

export interface CatalogResultsProps {
  filters: CatalogFilters
  onPage: (page: number) => void
  header?: React.ReactNode
}

/** Loading, error, empty and result states of a catalogue search (NFR-6). */
export function CatalogResults({ filters, onPage, header }: CatalogResultsProps) {
  const { data, isPending, isError, error, refetch, isPlaceholderData } = useCatalogSearch(filters)

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />

  return (
    <div className={`flex flex-col gap-4 transition-opacity ${isPlaceholderData ? 'opacity-60' : ''}`}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-600">{uk.catalog.results(data.totalItems)}</p>
        {header}
      </div>
      {data.items.length === 0 ? (
        <EmptyState title={uk.catalog.empty} description={uk.catalog.emptyHint} />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.items.map((item) => (
            <EquipmentCard key={item.id} item={item} />
          ))}
        </div>
      )}
      <Pagination page={data.page} totalPages={data.totalPages} onChange={onPage} />
    </div>
  )
}
