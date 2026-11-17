import { Link } from 'react-router'
import { AuthCard } from '@/features/auth/components/AuthCard'
import { ForgotPasswordForm } from '@/features/auth/components/ForgotPasswordForm'
import { uk } from '@/i18n/uk'

const t = uk.auth.forgot

export function ForgotPasswordPage() {
  return (
    <AuthCard
      title={t.title}
      footer={
        <Link to="/login" className="font-medium text-brand-700 hover:underline">
          {t.backToLogin}
        </Link>
      }
    >
      <ForgotPasswordForm />
    </AuthCard>
  )
}
