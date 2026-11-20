import { useParams } from 'react-router'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { PublicProfileCard } from '@/features/catalog/components/PublicProfileCard'
import { usePublicProfile } from '@/features/catalog/hooks/useCatalogQueries'

/** FR-19: a user's public profile. */
export function PublicProfilePage() {
  const id = Number(useParams().id)
  const { data, isPending, isError, error, refetch } = usePublicProfile(id)

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />
  return (
    <div className="mx-auto w-full max-w-3xl">
      <PublicProfileCard profile={data} />
    </div>
  )
}
