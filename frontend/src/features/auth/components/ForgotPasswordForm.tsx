import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { useForgotPassword } from '@/features/auth/hooks/usePasswordReset'
import { forgotPasswordSchema, type ForgotPasswordInput } from '@/features/auth/schemas/passwordSchemas'
import { uk } from '@/i18n/uk'

const t = uk.auth

export function ForgotPasswordForm() {
  const forgot = useForgotPassword()
  const form = useForm<ForgotPasswordInput>({ resolver: zodResolver(forgotPasswordSchema), defaultValues: { email: '' } })
  const { errors } = form.formState

  if (forgot.isSuccess) return <Alert tone="success">{t.forgot.sent}</Alert>

  return (
    <form onSubmit={form.handleSubmit((values) => forgot.mutate(values.email))} noValidate className="flex flex-col gap-3">
      <p className="text-sm text-slate-600">{t.forgot.text}</p>
      {forgot.isError && <Alert tone="error">{problemMessage(forgot.error)}</Alert>}
      <FormField label={t.fields.email} htmlFor="forgot-email" error={errors.email?.message}>
        <Input id="forgot-email" type="email" autoComplete="email" invalid={!!errors.email} {...form.register('email')} />
      </FormField>
      <Button type="submit" loading={forgot.isPending}>
        {t.forgot.submit}
      </Button>
    </form>
  )
}
