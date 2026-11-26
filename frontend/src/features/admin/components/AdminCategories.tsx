import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { LoadingState } from '@/components/ui/Spinner'
import type { AdminCategory } from '@/features/admin/catalogTypes'
import { CategoryDialog } from '@/features/admin/components/CategoryDialog'
import { DeleteCategoryDialog } from '@/features/admin/components/DeleteCategoryDialog'
import { useAdminCategories } from '@/features/admin/hooks/useAdminCatalog'
import { monthLabel } from '@/features/admin/format'
import { uk } from '@/i18n/uk'

const t = uk.adminCatalog.categories

type Dialogs = { kind: 'edit'; category: AdminCategory | null } | { kind: 'delete'; category: AdminCategory } | null

const season = (c: AdminCategory) =>
  c.seasonFrom === 1 && c.seasonTo === 12 ? t.allYear : t.season(monthLabel(c.seasonFrom), monthLabel(c.seasonTo))

/** FR-22 «Категорії»: list with seasons and listing counts; create, edit, delete (moving listings if any). */
export function AdminCategories() {
  const { data, isPending, isError, error, refetch } = useAdminCategories()
  const [dialog, setDialog] = useState<Dialogs>(null)
  const [version, setVersion] = useState(0)
  const open = (next: Dialogs) => {
    setVersion((v) => v + 1)
    setDialog(next)
  }

  if (isPending) return <LoadingState />
  if (isError) return <ErrorState error={error} onRetry={() => void refetch()} />

  return (
    <div className="flex flex-col gap-4">
      <Button className="self-start" onClick={() => open({ kind: 'edit', category: null })}>
        {t.add}
      </Button>
      {data.length === 0 && <EmptyState title={t.empty} />}
      <ul className="flex flex-col gap-2">
        {data.map((c) => (
          <li
            key={c.id}
            className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-white p-3 text-sm sm:flex-row sm:items-center"
          >
            <div className="flex min-w-0 flex-1 flex-col gap-1">
              <p className="font-semibold text-slate-900">{c.name}</p>
              <p className="text-slate-600">
                {season(c)} · {t.listings(c.listingCount)}
              </p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={() => open({ kind: 'edit', category: c })}>
                {t.edit}
              </Button>
              <Button variant="danger" onClick={() => open({ kind: 'delete', category: c })}>
                {t.delete}
              </Button>
            </div>
          </li>
        ))}
      </ul>
      {dialog?.kind === 'edit' && (
        <CategoryDialog key={version} open category={dialog.category} onClose={() => setDialog(null)} />
      )}
      {dialog?.kind === 'delete' && (
        <DeleteCategoryDialog
          key={version}
          open
          category={dialog.category}
          others={data.filter((c) => c.id !== dialog.category.id)}
          onClose={() => setDialog(null)}
        />
      )}
    </div>
  )
}
