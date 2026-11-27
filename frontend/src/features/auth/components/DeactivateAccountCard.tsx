import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { useDeactivateAccount } from '@/features/auth/hooks/useAccountMutations'
import { deactivateSchema, type DeactivateInput } from '@/features/auth/schemas/deactivateSchema'
import { BlockingBookings } from '@/features/booking/components/BlockingBookings'
import { uk } from '@/i18n/uk'

const t = uk.profile.deactivate

/** FR-3c: deactivation with password confirmation and an explicit confirm dialog. */
export function DeactivateAccountCard() {
  const navigate = useNavigate()
  const deactivate = useDeactivateAccount()
  const [pending, setPending] = useState<DeactivateInput | null>(null)
  const form = useForm<DeactivateInput>({ resolver: zodResolver(deactivateSchema), defaultValues: { password: '' } })
  const { errors } = form.formState

  const confirm = async () => {
    if (!pending) return
    try {
      await deactivate.mutateAsync(pending.password)
      navigate('/', { replace: true })
    } finally {
      setPending(null)
    }
  }

  return (
    <section className="rounded-lg border border-red-200 bg-surface p-6">
      <h2 className="text-lg font-semibold text-red-700">{t.title}</h2>
      <p className="mt-1 text-sm text-slate-600">{t.text}</p>
      {deactivate.isError && (
        <Alert tone="error" className="mt-3">
          {problemMessage(deactivate.error)}
          <BlockingBookings error={deactivate.error} />
        </Alert>
      )}
      <form onSubmit={form.handleSubmit(setPending)} noValidate className="mt-4 flex flex-col gap-3 sm:max-w-sm">
        <FormField label={t.passwordLabel} htmlFor="deactivate-password" error={errors.password?.message}>
          <Input
            id="deactivate-password"
            type="password"
            autoComplete="current-password"
            invalid={!!errors.password}
            {...form.register('password')}
          />
        </FormField>
        <Button type="submit" variant="danger" className="self-start">
          {t.submit}
        </Button>
      </form>
      <Dialog open={pending !== null} title={t.confirmTitle} onClose={() => setPending(null)}>
        <p className="text-sm text-slate-600">{t.text}</p>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setPending(null)} disabled={deactivate.isPending}>
            {t.cancel}
          </Button>
          <Button variant="danger" onClick={confirm} loading={deactivate.isPending}>
            {t.confirm}
          </Button>
        </div>
      </Dialog>
    </section>
  )
}
