import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { FormField } from '@/components/ui/FormField'
import { Select } from '@/components/ui/Select'
import type { AdminCategory } from '@/features/admin/catalogTypes'
import { useDeleteCategory } from '@/features/admin/hooks/useAdminCatalog'
import { uk } from '@/i18n/uk'

const t = uk.adminCatalog.categories

export interface DeleteCategoryDialogProps {
  open: boolean
  category: AdminCategory
  others: AdminCategory[]
  onClose: () => void
}

/** FR-22 (D-46): an empty category is deleted; one with listings needs a category to move them to. */
export function DeleteCategoryDialog({ open, category, others, onClose }: DeleteCategoryDialogProps) {
  const [target, setTarget] = useState('')
  const remove = useDeleteCategory(category.id)
  const needsTarget = category.listingCount > 0

  return (
    <Dialog open={open} title={t.deleteTitle} onClose={onClose}>
      <div className="flex flex-col gap-3">
        <p className="text-sm text-slate-600">{needsTarget ? t.deleteMove(category.listingCount) : t.deleteEmpty}</p>
        {needsTarget && (
          <FormField label={t.target} htmlFor="category-target">
            <Select
              id="category-target"
              placeholder={t.chooseTarget}
              options={others.map((c) => ({ value: String(c.id), label: c.name }))}
              value={target}
              onChange={(e) => setTarget(e.target.value)}
            />
          </FormField>
        )}
        {remove.isError && <Alert tone="error">{problemMessage(remove.error)}</Alert>}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            {t.back}
          </Button>
          <Button
            variant="danger"
            loading={remove.isPending}
            disabled={needsTarget && target === ''}
            onClick={() => remove.mutate(target ? Number(target) : undefined, { onSuccess: onClose })}
          >
            {t.delete}
          </Button>
        </div>
      </div>
    </Dialog>
  )
}
