import type { ReactNode } from 'react'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import { Pagination } from '@/features/catalog/components/Pagination'
import type { Page } from '@/features/catalog/types'

export interface AdminListProps<T> {
  query: {
    data?: Page<T>
    isPending: boolean
    isError: boolean
    error: unknown
    refetch: () => unknown
  }
  empty: string
  page: number
  onPage: (page: number) => void
  row: (item: T) => ReactNode
}

/** Loading / error / empty states, the rows and paging for the admin tabs. */
export function AdminList<T>({ query, empty, page, onPage, row }: AdminListProps<T>) {
  if (query.isPending) return <LoadingState />
  if (query.isError || !query.data) return <ErrorState error={query.error} onRetry={() => void query.refetch()} />
  if (query.data.items.length === 0) return <EmptyState title={empty} />
  return (
    <div className="flex flex-col gap-4">
      <ul className="flex flex-col gap-2">{query.data.items.map(row)}</ul>
      <Pagination page={page} totalPages={query.data.totalPages} onChange={onPage} />
    </div>
  )
}
