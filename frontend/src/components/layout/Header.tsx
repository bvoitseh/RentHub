import { NavLink } from 'react-router'
import { MobileNav } from '@/components/layout/MobileNav'
import { ThemeToggle } from '@/components/layout/ThemeToggle'
import { UserMenu } from '@/features/auth/components/UserMenu'
import { uk } from '@/i18n/uk'

/** Top bar: ☰ (phones) and logo, theme switch and the user's links; page links live in the side menu (D-53). */
export function Header() {
  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-surface/90 backdrop-blur">
      <div className="flex h-14 w-full items-center justify-between gap-4 px-4 sm:px-6">
        <div className="flex items-center gap-2">
          <MobileNav />
          <NavLink to="/" aria-label={uk.app.name} className="flex items-center gap-2 text-lg font-bold text-brand-700">
            <img src="/favicon.svg" alt="" className="h-7 w-7" />
            <span className="hidden sm:inline">{uk.app.name}</span>
          </NavLink>
        </div>
        <div className="flex items-center gap-3">
          <ThemeToggle />
          <UserMenu />
        </div>
      </div>
    </header>
  )
}
