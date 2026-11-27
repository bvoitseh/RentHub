import { Link, useLocation, useParams } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { BlocksManager } from '@/features/booking/components/BlocksManager'
import { useEquipmentCard } from '@/features/catalog/hooks/useCatalogQueries'
import { FormSection } from '@/features/listings/components/FormSection'
import { ListingForm } from '@/features/listings/components/ListingForm'
import { PhotoManager } from '@/features/listings/components/PhotoManager'
import { VideoManager } from '@/features/listings/components/VideoManager'
import { toListingInput } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'
import { NotFoundPage } from '@/pages/NotFoundPage'

const t = uk.listings

/** FR-4a, FR-5, FR-8: edit an own listing, manage its photos and close dates in its calendar. */
export function ListingEditPage() {
  const id = Number(useParams().id)
  const created = Boolean((useLocation().state as { created?: boolean } | null)?.created)
  const { data, isPending, isError, error, refetch } = useEquipmentCard(id)

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  if (!data.mine) return <NotFoundPage />

  return (
    <div className="mx-auto flex w-full max-w-3xl flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">{t.editTitle}</h1>
        <Link to="/my/listings" className="text-sm font-medium text-brand-700 hover:underline">
          ← {t.title}
        </Link>
      </div>
      {created && <Alert tone="success">{t.created}</Alert>}
      <FormSection title={t.sections.photos}>
        <PhotoManager listingId={data.id} photos={data.photos} />
        <VideoManager listingId={data.id} video={data.video} />
      </FormSection>
      <ListingForm key={data.id} initial={toListingInput(data)} listingId={data.id} onSaved={() => undefined} />
      <FormSection title={uk.booking.blocks.title}>
        <BlocksManager equipmentId={data.id} />
      </FormSection>
    </div>
  )
}
