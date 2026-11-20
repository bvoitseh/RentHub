import { useNavigate } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { ListingForm } from '@/features/listings/components/ListingForm'
import { emptyListing } from '@/features/listings/schemas/listingSchema'
import { uk } from '@/i18n/uk'

/** FR-4: new listing; after saving, the owner continues on the edit page to add photos. */
export function ListingCreatePage() {
  const navigate = useNavigate()
  const { data: user } = useCurrentUser()

  return (
    <div className="mx-auto flex w-full max-w-3xl flex-col gap-4">
      <h1 className="text-2xl font-bold text-slate-900">{uk.listings.createTitle}</h1>
      {user?.emailVerified ? (
        <>
          <Alert tone="info">{uk.listings.photos.afterCreate}</Alert>
          <ListingForm
            initial={emptyListing}
            onSaved={(item) => navigate(`/my/listings/${item.id}/edit`, { replace: true, state: { created: true } })}
          />
        </>
      ) : (
        <Alert tone="warning">{uk.listings.verifyFirst}</Alert>
      )}
    </div>
  )
}
