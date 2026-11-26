import { useParams } from 'react-router'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { PublicProfileCard } from '@/features/catalog/components/PublicProfileCard'
import { usePublicProfile } from '@/features/catalog/hooks/useCatalogQueries'
import { UserReviews } from '@/features/reviews/components/UserReviews'

/** FR-19: a user's public profile with their published reviews (the user can reply to them here). */
export function PublicProfilePage() {
  const id = Number(useParams().id)
  const { data, isPending, isError, error, refetch } = usePublicProfile(id)

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  return (
    <div className="mx-auto flex w-full max-w-3xl flex-col gap-6">
      <PublicProfileCard profile={data} />
      <UserReviews userId={data.id} />
    </div>
  )
}
