import { useEffect } from 'react'
import { Link, useSearchParams } from 'react-router'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { LoadingState } from '@/components/ui/Spinner'
import { AuthCard } from '@/features/auth/components/AuthCard'
import { useVerifyEmail } from '@/features/auth/hooks/useEmailVerification'
import { uk } from '@/i18n/uk'

const t = uk.auth.verify

/** Landing page of the verification link: consumes the token once on mount. */
export function VerifyEmailPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const verify = useVerifyEmail()
  const { mutate } = verify

  useEffect(() => {
    if (token) mutate(token)
  }, [token, mutate])

  let content
  if (!token) content = <Alert tone="error">{t.missingToken}</Alert>
  else if (verify.isError) content = <Alert tone="error">{problemMessage(verify.error)}</Alert>
  else if (verify.isSuccess) content = <Alert tone="success">{t.success}</Alert>
  else content = <LoadingState label={t.checking} />

  return (
    <AuthCard
      title={t.title}
      footer={
        <Link to="/" className="font-medium text-brand-700 hover:underline">
          {uk.notFound.backHome}
        </Link>
      }
    >
      {content}
    </AuthCard>
  )
}
