import { useState } from 'react'
import { Link } from 'react-router'
import { AdminList } from '@/features/admin/components/AdminList'
import { AdminSearch } from '@/features/admin/components/AdminSearch'
import { BlockToggle } from '@/features/admin/components/BlockToggle'
import { useAdminUsers } from '@/features/admin/hooks/useAdmin'
import type { AdminUser } from '@/features/admin/types'
import { uk } from '@/i18n/uk'

const t = uk.admin

function UserRow({ user: u }: { user: AdminUser }) {
  return (
    <li className="flex flex-col gap-2 rounded-lg border border-slate-200 bg-surface p-3 text-sm sm:flex-row sm:items-center">
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <p className="flex flex-wrap items-center gap-2">
          <Link to={`/users/${u.id}`} className="font-semibold text-slate-900 hover:text-brand-700">
            {u.name}
          </Link>
          {u.blocked && <span className="rounded-full bg-red-50 px-2 py-0.5 text-xs text-red-700">{t.blocked}</span>}
          {u.deactivated && <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs">{t.deactivated}</span>}
          {!u.emailVerified && <span className="text-xs text-amber-800">{t.notVerified}</span>}
        </p>
        <p className="text-slate-600">
          {u.email}
          {u.phone && ` · ${u.phone}`}
          {u.city && ` · ${u.city}`} · {t.complaintsCount(u.complaintCount)}
        </p>
      </div>
      <BlockToggle kind="users" id={u.id} blocked={u.blocked} />
    </li>
  )
}

/** FR-22 «Користувачі»: search by name or e-mail, block / unblock (FR-20). */
export function AdminUsers() {
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)
  const query = useAdminUsers(q, page)
  return (
    <div className="flex flex-col gap-4">
      <AdminSearch
        placeholder={t.searchUsers}
        onSearch={(value) => {
          setQ(value)
          setPage(0)
        }}
      />
      <AdminList
        query={query}
        empty={t.noUsers}
        page={page}
        onPage={setPage}
        row={(u) => <UserRow key={u.id} user={u} />}
      />
    </div>
  )
}
