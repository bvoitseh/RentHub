import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { useResetPassword } from '@/features/auth/hooks/usePasswordReset'
import { resetPasswordSchema, type ResetPasswordInput } from '@/features/auth/schemas/passwordSchemas'
import { uk } from '@/i18n/uk'

const t = uk.auth

export function ResetPasswordForm({ token }: { token: string }) {
  const reset = useResetPassword()
  const form = useForm<ResetPasswordInput>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { password: '', passwordConfirm: '' },
  })
  const { errors } = form.formState

  if (reset.isSuccess) {
    return (
      <Alert tone="success">
        {t.reset.success}{' '}
        <Link to="/login" className="font-medium underline">
          {t.login.submit}
        </Link>
      </Alert>
    )
  }

  return (
    <form
      onSubmit={form.handleSubmit((values) => reset.mutate({ token, password: values.password }))}
      noValidate
      className="flex flex-col gap-3"
    >
      {reset.isError && <Alert tone="error">{problemMessage(reset.error)}</Alert>}
      <FormField label={t.fields.newPassword} htmlFor="reset-password" error={errors.password?.message}>
        <Input
          id="reset-password"
          type="password"
          autoComplete="new-password"
          invalid={!!errors.password}
          {...form.register('password')}
        />
      </FormField>
      <FormField label={t.fields.passwordConfirm} htmlFor="reset-confirm" error={errors.passwordConfirm?.message}>
        <Input
          id="reset-confirm"
          type="password"
          autoComplete="new-password"
          invalid={!!errors.passwordConfirm}
          {...form.register('passwordConfirm')}
        />
      </FormField>
      <Button type="submit" loading={reset.isPending}>
        {t.reset.submit}
      </Button>
    </form>
  )
}
