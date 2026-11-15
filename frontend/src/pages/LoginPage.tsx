import { Link, useLocation } from 'react-router'
import { AuthCard } from '@/features/auth/components/AuthCard'
import { LoginForm } from '@/features/auth/components/LoginForm'
import { uk } from '@/i18n/uk'

const t = uk.auth.login

export function LoginPage() {
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  return (
    <AuthCard
      title={t.title}
      footer={
        <>
          {t.noAccount}{' '}
          <Link to="/register" className="font-medium text-brand-700 hover:underline">
            {t.register}
          </Link>
        </>
      }
    >
      <LoginForm redirectTo={from} />
    </AuthCard>
  )
}
