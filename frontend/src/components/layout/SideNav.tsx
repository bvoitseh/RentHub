import { NavLink } from 'react-router'
import { navItems } from '@/components/layout/navItems'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { uk } from '@/i18n/uk'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

/** The page links the current user may see (D-53); `onNavigate` lets the drawer close after a choice. */
export function SideNav({ onNavigate }: { onNavigate?: () => void }) {
  const { data: user } = useCurrentUser()
  const items = navItems.filter((item) => !item.roles || (user && item.roles.includes(user.role)))

  return (
    <nav aria-label={uk.nav.main}>
      <ul className="flex flex-col gap-1">
        {items.map((item) => (
          <li key={item.to}>
            <NavLink to={item.to} end={item.end} className={linkClass} onClick={onNavigate}>
              {item.icon}
              {item.label}
              {item.badge && <item.badge />}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
