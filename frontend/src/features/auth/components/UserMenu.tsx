import { Link, NavLink } from 'react-router'
import { Button } from '@/components/ui/Button'
import { useLogout } from '@/features/auth/hooks/useAuthMutations'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { uk } from '@/i18n/uk'

const t = uk.auth.menu

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

/** Header area: login/register links for guests, name + profile + logout for users. */
export function UserMenu() {
  const { data: user, isPending } = useCurrentUser()
  const logout = useLogout()

  if (isPending) return <span className="h-8 w-24 animate-pulse rounded-md bg-slate-100" aria-hidden />

  if (!user) {
    return (
      <div className="flex items-center gap-1">
        <NavLink to="/login" className={linkClass}>
          {t.login}
        </NavLink>
        <Link to="/register" className="rounded-md bg-brand-600 px-3 py-2 text-sm font-medium text-on-accent hover:bg-brand-700">
          {t.register}
        </Link>
      </div>
    )
  }

  return (
    <div className="flex items-center gap-1">
      <NavLink to="/profile" className={linkClass} title={t.profile}>
        {user.name}
      </NavLink>
      <Button
        variant="ghost"
        loading={logout.isPending}
        // A full page load, like after deactivation: an in-app navigate loses the race with a protected page's guard.
        onClick={() => logout.mutate(undefined, { onSettled: () => window.location.replace('/') })}
      >
        {t.logout}
      </Button>
    </div>
  )
}
