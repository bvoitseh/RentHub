import { Button } from '@/components/ui/Button'
import { uk } from '@/i18n/uk'

export interface PaginationProps {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

/** Previous / next with "page X of Y"; `page` is 0-based like the API. */
export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  if (totalPages <= 1) return null
  const t = uk.catalog.pagination
  return (
    <nav className="flex items-center justify-center gap-3" aria-label={t.page(page + 1, totalPages)}>
      <Button variant="secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
        {t.prev}
      </Button>
      <span className="text-sm text-slate-600">{t.page(page + 1, totalPages)}</span>
      <Button variant="secondary" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
        {t.next}
      </Button>
    </nav>
  )
}
