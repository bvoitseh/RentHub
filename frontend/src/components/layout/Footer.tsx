import { Link } from 'react-router'
import { BackendStatus } from '@/components/layout/BackendStatus'
import { uk } from '@/i18n/uk'

export function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-2 px-4 py-4 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <span>
          © {new Date().getFullYear()} {uk.app.name}. {uk.app.tagline}{' '}
          <Link to="/privacy" className="text-brand-700 hover:underline">
            {uk.privacy.link}
          </Link>
        </span>
        <BackendStatus />
      </div>
    </footer>
  )
}
