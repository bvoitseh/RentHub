import { Link } from 'react-router'
import { AuthCard } from '@/features/auth/components/AuthCard'
import { RegisterForm } from '@/features/auth/components/RegisterForm'
import { uk } from '@/i18n/uk'

const t = uk.auth.register

export function RegisterPage() {
  return (
    <AuthCard
      title={t.title}
      footer={
        <>
          {t.haveAccount}{' '}
          <Link to="/login" className="font-medium text-brand-700 hover:underline">
            {t.login}
          </Link>
        </>
      }
    >
      <RegisterForm />
    </AuthCard>
  )
}
