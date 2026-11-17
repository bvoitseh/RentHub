import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { isProblem, problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { RestoreAccountDialog } from '@/features/auth/components/RestoreAccountDialog'
import { useLogin } from '@/features/auth/hooks/useAuthMutations'
import { loginSchema, type LoginInput } from '@/features/auth/schemas/loginSchema'
import { uk } from '@/i18n/uk'

const t = uk.auth

export function LoginForm({ redirectTo = '/' }: { redirectTo?: string }) {
  const navigate = useNavigate()
  const login = useLogin()
  const [restoreFor, setRestoreFor] = useState<LoginInput | null>(null)
  const form = useForm<LoginInput>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } })
  const { errors } = form.formState

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      await login.mutateAsync(values)
      navigate(redirectTo, { replace: true })
    } catch (error) {
      if (isProblem(error) && error.code === 'auth.deactivated') setRestoreFor(values)
    }
  })

  const showError = login.isError && !restoreFor

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-3">
      {showError && <Alert tone="error">{problemMessage(login.error)}</Alert>}
      <FormField label={t.fields.email} htmlFor="login-email" error={errors.email?.message}>
        <Input id="login-email" type="email" autoComplete="email" invalid={!!errors.email} {...form.register('email')} />
      </FormField>
      <FormField label={t.fields.password} htmlFor="login-password" error={errors.password?.message}>
        <Input
          id="login-password"
          type="password"
          autoComplete="current-password"
          invalid={!!errors.password}
          {...form.register('password')}
        />
      </FormField>
      <div className="flex items-center justify-between">
        <Link to="/forgot-password" className="text-sm text-brand-700 hover:underline">
          {t.login.forgot}
        </Link>
        <Button type="submit" loading={login.isPending}>
          {t.login.submit}
        </Button>
      </div>
      <RestoreAccountDialog
        credentials={restoreFor}
        onClose={() => setRestoreFor(null)}
        onRestored={() => navigate(redirectTo, { replace: true })}
      />
    </form>
  )
}
