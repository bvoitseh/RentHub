import { NavLink } from 'react-router'
import { uk } from '@/i18n/uk'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

export function Header() {
  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-14 w-full max-w-6xl items-center justify-between px-4 sm:px-6">
        <NavLink to="/" className="flex items-center gap-2 text-lg font-bold text-brand-700">
          <img src="/favicon.svg" alt="" className="h-7 w-7" />
          {uk.app.name}
        </NavLink>
        <nav aria-label={uk.nav.main} className="flex items-center gap-1">
          <NavLink to="/" end className={navLinkClass}>
            {uk.nav.home}
          </NavLink>
        </nav>
      </div>
    </header>
  )
}
