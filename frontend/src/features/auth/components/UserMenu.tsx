import { Link, NavLink, useNavigate } from 'react-router'
import { IconButton } from '@/components/ui/IconButton'
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
  const navigate = useNavigate()
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
      <IconButton
        label={t.logout}
        tone="danger"
        disabled={logout.isPending}
        onClick={() => logout.mutate(undefined, { onSettled: () => navigate('/', { replace: true }) })}
      >
        <svg
          className="h-5 w-5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" />
        </svg>
      </IconButton>
    </div>
  )
}
