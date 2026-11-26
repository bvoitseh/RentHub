import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import type { AdminCategory } from '@/features/admin/catalogTypes'
import { useSaveCategory } from '@/features/admin/hooks/useAdminCatalog'
import { months } from '@/features/admin/format'
import { uk } from '@/i18n/uk'

const t = uk.adminCatalog.categories
const options = months.map((label, i) => ({ value: String(i + 1), label }))

export interface CategoryDialogProps {
  open: boolean
  /** null creates a new category. */
  category: AdminCategory | null
  onClose: () => void
}

/** FR-22: name and season of a category; a season may wrap over New Year (from > to). */
export function CategoryDialog({ open, category, onClose }: CategoryDialogProps) {
  const [name, setName] = useState(category?.name ?? '')
  const [from, setFrom] = useState(String(category?.seasonFrom ?? 1))
  const [to, setTo] = useState(String(category?.seasonTo ?? 12))
  const save = useSaveCategory(category?.id ?? null)
  const valid = name.trim().length >= 2 && name.trim().length <= 100

  return (
    <Dialog open={open} title={category ? t.editTitle : t.createTitle} onClose={onClose}>
      <div className="flex flex-col gap-3">
        <FormField label={t.name} htmlFor="category-name">
          <Input id="category-name" maxLength={100} value={name} onChange={(e) => setName(e.target.value)} />
        </FormField>
        <div className="grid grid-cols-2 gap-3">
          <FormField label={t.seasonFrom} htmlFor="category-from">
            <Select id="category-from" options={options} value={from} onChange={(e) => setFrom(e.target.value)} />
          </FormField>
          <FormField label={t.seasonTo} htmlFor="category-to">
            <Select id="category-to" options={options} value={to} onChange={(e) => setTo(e.target.value)} />
          </FormField>
        </div>
        {save.isError && <Alert tone="error">{problemMessage(save.error)}</Alert>}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            {t.back}
          </Button>
          <Button
            loading={save.isPending}
            disabled={!valid}
            onClick={() =>
              save.mutate({ name: name.trim(), seasonFrom: Number(from), seasonTo: Number(to) }, { onSuccess: onClose })
            }
          >
            {t.save}
          </Button>
        </div>
      </div>
    </Dialog>
  )
}
