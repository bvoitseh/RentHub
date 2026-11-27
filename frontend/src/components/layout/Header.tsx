import { NavLink } from 'react-router'
import { ThemeToggle } from '@/components/layout/ThemeToggle'
import { UserMenu } from '@/features/auth/components/UserMenu'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { uk } from '@/i18n/uk'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

export function Header() {
  const { data: user } = useCurrentUser()

  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-surface/90 backdrop-blur">
      <div className="mx-auto flex h-14 w-full max-w-6xl items-center justify-between gap-4 px-4 sm:px-6">
        <div className="flex items-center gap-4">
          <NavLink to="/" className="flex items-center gap-2 text-lg font-bold text-brand-700">
            <img src="/favicon.svg" alt="" className="h-7 w-7" />
            {uk.app.name}
          </NavLink>
          <nav aria-label={uk.nav.main} className="hidden items-center gap-1 sm:flex">
            <NavLink to="/" end className={navLinkClass}>
              {uk.nav.home}
            </NavLink>
            <NavLink to="/catalog" className={navLinkClass}>
              {uk.catalog.nav}
            </NavLink>
            {user?.role === 'USER' && (
              <>
                <NavLink to="/my/bookings" className={navLinkClass}>
                  {uk.booking.nav.mine}
                </NavLink>
                <NavLink to="/my/listings" className={navLinkClass}>
                  {uk.listings.nav}
                </NavLink>
                <NavLink to="/my/requests" className={navLinkClass}>
                  {uk.booking.nav.incoming}
                </NavLink>
              </>
            )}
            {user?.role === 'ADMIN' && (
              <NavLink to="/admin" className={navLinkClass}>
                {uk.admin.nav}
              </NavLink>
            )}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          <ThemeToggle />
          <UserMenu />
        </div>
      </div>
    </header>
  )
}
