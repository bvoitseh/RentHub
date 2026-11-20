import { Link, useParams } from 'react-router'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { EquipmentView } from '@/features/catalog/components/EquipmentView'
import { useEquipmentCard } from '@/features/catalog/hooks/useCatalogQueries'
import { uk } from '@/i18n/uk'

/** FR-6: the equipment card. */
export function EquipmentPage() {
  const id = Number(useParams().id)
  const { data, isPending, isError, error, refetch } = useEquipmentCard(id)

  return (
    <div className="flex flex-col gap-4">
      <Link to="/catalog" className="self-start text-sm font-medium text-brand-700 hover:underline">
        ← {uk.catalog.card.back}
      </Link>
      {isPending ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState error={error} onRetry={() => void refetch()} />
      ) : (
        <EquipmentView item={data} />
      )}
    </div>
  )
}
