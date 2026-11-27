import { useTheme } from '@/hooks/useTheme'
import { uk } from '@/i18n/uk'

const icon = 'h-3.5 w-3.5'

/** Header switch between the light and the dark theme (D-50). */
export function ThemeToggle() {
  const { theme, toggle } = useTheme()
  const dark = theme === 'dark'

  return (
    <button
      type="button"
      role="switch"
      aria-checked={dark}
      aria-label={uk.nav.darkTheme}
      title={dark ? uk.nav.lightThemeHint : uk.nav.darkThemeHint}
      onClick={toggle}
      className="relative inline-flex h-7 w-12 shrink-0 items-center rounded-full border border-slate-300 bg-slate-100 transition-colors focus-visible:outline-2 focus-visible:outline-brand-600"
    >
      <span
        className={`absolute top-0.5 left-0.5 flex h-5.5 w-5.5 items-center justify-center rounded-full bg-surface text-slate-700 shadow transition-transform ${dark ? 'translate-x-5' : ''}`}
      >
        {dark ? (
          <svg className={icon} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
          </svg>
        ) : (
          <svg className={icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <circle cx="12" cy="12" r="4" />
            <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
          </svg>
        )}
      </span>
    </button>
  )
}
