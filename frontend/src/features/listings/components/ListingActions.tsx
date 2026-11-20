import { useState } from 'react'
import { Link } from 'react-router'
import { problemMessage } from '@/api/problem'
import { Button } from '@/components/ui/Button'
import type { EquipmentSummary } from '@/features/catalog/types'
import { ConfirmDialog } from '@/features/listings/components/ConfirmDialog'
import { useArchiveListing, useDeleteListing } from '@/features/listings/hooks/useListings'
import { uk } from '@/i18n/uk'

const t = uk.listings

/** Open, edit, archive (FR-5) and delete with confirmation; errors from the backend are shown inline. */
export function ListingActions({ item }: { item: EquipmentSummary }) {
  const [dialog, setDialog] = useState<'archive' | 'delete' | null>(null)
  const archive = useArchiveListing()
  const remove = useDeleteListing()
  const error = archive.error ?? remove.error
  const close = () => setDialog(null)

  return (
    <div className="flex flex-col items-end gap-1">
      <div className="flex flex-wrap justify-end gap-1">
        <Link to={`/equipment/${item.id}`} className="rounded-md px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">
          {t.open}
        </Link>
        <Link to={`/my/listings/${item.id}/edit`} className="rounded-md px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">
          {t.edit}
        </Link>
        {item.status === 'ACTIVE' && (
          <Button variant="ghost" onClick={() => setDialog('archive')}>
            {t.archive}
          </Button>
        )}
        <Button variant="ghost" className="text-red-700" onClick={() => setDialog('delete')}>
          {t.delete}
        </Button>
      </div>
      {error && <p className="text-xs text-red-600">{problemMessage(error)}</p>}
      <ConfirmDialog
        open={dialog === 'archive'}
        title={t.archiveConfirmTitle}
        text={t.archiveConfirm}
        confirmLabel={t.archive}
        loading={archive.isPending}
        onConfirm={() => archive.mutate(item.id, { onSettled: close })}
        onClose={close}
      />
      <ConfirmDialog
        open={dialog === 'delete'}
        title={t.deleteConfirmTitle}
        text={t.deleteConfirm}
        confirmLabel={t.delete}
        danger
        loading={remove.isPending}
        onConfirm={() => remove.mutate(item.id, { onSettled: close })}
        onClose={close}
      />
    </div>
  )
}
