import { Link } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { MyListingRow } from '@/features/listings/components/MyListingRow'
import { useMyListings } from '@/features/listings/hooks/useListings'
import { uk } from '@/i18n/uk'

const t = uk.listings
const createLink = 'rounded-md bg-brand-600 px-4 py-2 text-sm font-medium text-on-accent hover:bg-brand-700'

/** The owner's dashboard: own listings in every status (rendered inside RequireAuth). */
export function MyListingsPage() {
  const { data: user } = useCurrentUser()
  const { data, isPending, isError, error, refetch } = useMyListings()
  const verified = Boolean(user?.emailVerified)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">{t.title}</h1>
        {verified && (
          <Link to="/my/listings/new" className={createLink}>
            {t.create}
          </Link>
        )}
      </div>
      {!verified && <Alert tone="warning">{t.verifyFirst}</Alert>}
      {isPending ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState error={error} onRetry={() => void refetch()} />
      ) : data.length === 0 ? (
        <EmptyState title={t.empty} description={t.emptyHint} />
      ) : (
        <ul className="flex flex-col gap-3">
          {data.map((item) => (
            <MyListingRow key={item.id} item={item} />
          ))}
        </ul>
      )}
    </div>
  )
}
