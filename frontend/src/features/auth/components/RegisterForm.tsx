import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { applyServerErrors } from '@/api/formErrors'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { OwnerTypeFields } from '@/features/auth/components/OwnerTypeFields'
import { PhoneInput } from '@/features/auth/components/PhoneInput'
import { useRegister } from '@/features/auth/hooks/useAuthMutations'
import { toApiPhone } from '@/features/auth/phone'
import { registerDefaults, registerSchema, type RegisterInput } from '@/features/auth/schemas/registerSchema'
import type { RegisterPayload } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

const t = uk.auth

function toPayload(values: RegisterInput): RegisterPayload {
  const base = { name: values.name, email: values.email, phone: toApiPhone(values.phone), password: values.password, city: values.city }
  const company = values.ownerType === 'COMPANY' ? { companyName: values.companyName, edrpou: values.edrpou } : {}
  return { ...base, ownerType: values.ownerType, ...company }
}

export function RegisterForm() {
  const navigate = useNavigate()
  const register = useRegister()
  const form = useForm<RegisterInput>({ resolver: zodResolver(registerSchema), defaultValues: registerDefaults })
  const { errors } = form.formState
  const ownerType = useWatch({ control: form.control, name: 'ownerType' })

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      await register.mutateAsync(toPayload(values))
      navigate('/', { replace: true })
    } catch (error) {
      applyServerErrors(error, form.setError)
    }
  })

  const field = (name: keyof RegisterInput, label: string, props: React.ComponentProps<typeof Input> = {}, hint?: string) => (
    <FormField label={label} htmlFor={`reg-${name}`} error={errors[name]?.message} hint={hint}>
      <Input id={`reg-${name}`} invalid={!!errors[name]} {...props} {...form.register(name)} />
    </FormField>
  )

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-3">
      {register.isError && <Alert tone="error">{problemMessage(register.error)}</Alert>}
      {field('name', t.fields.name, { autoComplete: 'name' })}
      {field('email', t.fields.email, { type: 'email', autoComplete: 'email' })}
      <FormField label={t.fields.phone} htmlFor="reg-phone" error={errors.phone?.message} hint={t.register.phoneHint}>
        <Controller
          control={form.control}
          name="phone"
          render={({ field: { ref, ...phone } }) => <PhoneInput id="reg-phone" ref={ref} invalid={!!errors.phone} {...phone} />}
        />
      </FormField>
      {field('city', t.fields.city, { autoComplete: 'address-level2' })}
      {field('password', t.fields.password, { type: 'password', autoComplete: 'new-password' }, t.register.passwordHint)}
      {field('passwordConfirm', t.fields.passwordConfirm, { type: 'password', autoComplete: 'new-password' })}
      <OwnerTypeFields register={form.register} errors={errors} ownerType={ownerType} idPrefix="reg" />
      <Button type="submit" loading={register.isPending} className="mt-2">
        {t.register.submit}
      </Button>
    </form>
  )
}
