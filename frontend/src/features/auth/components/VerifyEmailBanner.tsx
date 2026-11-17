import { problemMessage } from '@/api/problem'
import { Button } from '@/components/ui/Button'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useResendVerification } from '@/features/auth/hooks/useEmailVerification'
import { uk } from '@/i18n/uk'

const t = uk.auth.verify

/** FR-1: shown to logged-in users whose e-mail is not verified yet, with a "resend" action. */
export function VerifyEmailBanner() {
  const { data: user } = useCurrentUser()
  const resend = useResendVerification()

  if (!user || user.emailVerified) return null

  return (
    <div className="border-b border-amber-200 bg-amber-50 text-amber-900">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-2 px-4 py-2 text-sm sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <span>{resend.isSuccess ? t.resent : resend.isError ? problemMessage(resend.error) : t.banner}</span>
        {!resend.isSuccess && (
          <Button variant="secondary" onClick={() => resend.mutate()} loading={resend.isPending}>
            {t.resend}
          </Button>
        )}
      </div>
    </div>
  )
}
