import { useState } from 'react'
import { Link } from 'react-router'
import { AdminList } from '@/features/admin/components/AdminList'
import { AdminSearch } from '@/features/admin/components/AdminSearch'
import { BlockToggle } from '@/features/admin/components/BlockToggle'
import { useAdminEquipment } from '@/features/admin/hooks/useAdmin'
import type { AdminEquipment as Item } from '@/features/admin/types'
import { formatPrice } from '@/features/catalog/format'
import { uk } from '@/i18n/uk'

const t = uk.admin

function EquipmentRow({ item: e }: { item: Item }) {
  return (
    <li className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-white p-3 text-sm sm:flex-row sm:items-center">
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <p className="flex flex-wrap items-center gap-2">
          <Link to={`/equipment/${e.id}`} className="font-semibold text-slate-900 hover:text-brand-700">
            {e.title}
          </Link>
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs">{uk.listings.statuses[e.status]}</span>
        </p>
        <p className="text-slate-600">
          {e.categoryName} · {formatPrice(e.pricePerDay)} {uk.catalog.perDay} ·{' '}
          <Link to={`/users/${e.ownerId}`} className="text-brand-700 hover:underline">
            {e.ownerName}
          </Link>
          {e.ownerBlocked && <span className="text-red-700"> ({t.ownerBlocked})</span>}
        </p>
      </div>
      <BlockToggle kind="equipment" id={e.id} blocked={e.status === 'BLOCKED'} />
    </li>
  )
}

/** FR-22 «Оголошення»: every listing in any status, search by title, block / unblock (FR-20). */
export function AdminEquipment() {
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)
  const query = useAdminEquipment(q, page)
  return (
    <div className="flex flex-col gap-4">
      <AdminSearch
        placeholder={t.searchEquipment}
        onSearch={(value) => {
          setQ(value)
          setPage(0)
        }}
      />
      <AdminList
        query={query}
        empty={t.noEquipment}
        page={page}
        onPage={setPage}
        row={(e) => <EquipmentRow key={e.id} item={e} />}
      />
    </div>
  )
}
