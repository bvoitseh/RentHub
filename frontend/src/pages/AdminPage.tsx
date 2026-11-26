import { Navigate, NavLink, useParams } from 'react-router'
import { AdminComplaints } from '@/features/admin/components/AdminComplaints'
import { AdminEquipment } from '@/features/admin/components/AdminEquipment'
import { AdminUsers } from '@/features/admin/components/AdminUsers'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { uk } from '@/i18n/uk'

const tabs = {
  complaints: AdminComplaints,
  users: AdminUsers,
  equipment: AdminEquipment,
} as const
type Tab = keyof typeof tabs

const tabClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100'}`

/** FR-22: the admin area with the «Скарги», «Користувачі» and «Оголошення» tabs (ADMIN only, NFR-11). */
export function AdminPage() {
  const tab = useParams().tab
  if (!tab) return <Navigate to="/admin/complaints" replace />
  if (!(tab in tabs)) return <NotFoundPage />
  const Content = tabs[tab as Tab]

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold text-slate-900">{uk.admin.title}</h1>
      <nav className="flex flex-wrap gap-1 border-b border-slate-200 pb-2">
        {(Object.keys(tabs) as Tab[]).map((key) => (
          <NavLink key={key} to={`/admin/${key}`} className={tabClass}>
            {uk.admin.tabs[key]}
          </NavLink>
        ))}
      </nav>
      <Content />
    </div>
  )
}
