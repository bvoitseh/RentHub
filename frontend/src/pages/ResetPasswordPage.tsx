import { Link, useSearchParams } from 'react-router'
import { Alert } from '@/components/ui/Alert'
import { AuthCard } from '@/features/auth/components/AuthCard'
import { ResetPasswordForm } from '@/features/auth/components/ResetPasswordForm'
import { uk } from '@/i18n/uk'

const t = uk.auth

export function ResetPasswordPage() {
  const [params] = useSearchParams()
  const token = params.get('token')

  return (
    <AuthCard
      title={t.reset.title}
      footer={
        <Link to="/login" className="font-medium text-brand-700 hover:underline">
          {t.forgot.backToLogin}
        </Link>
      }
    >
      {token ? <ResetPasswordForm token={token} /> : <Alert tone="error">{t.reset.missingToken}</Alert>}
    </AuthCard>
  )
}
