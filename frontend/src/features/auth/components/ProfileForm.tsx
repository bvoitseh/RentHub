import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { applyServerErrors } from '@/api/formErrors'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { SettlementPicker } from '@/features/address/components/SettlementPicker'
import { toSettlementValue } from '@/features/address/types'
import { OwnerTypeFields } from '@/features/auth/components/OwnerTypeFields'
import { PhoneInput } from '@/features/auth/components/PhoneInput'
import { useUpdateProfile } from '@/features/auth/hooks/useAccountMutations'
import { profileSchema, type ProfileInput } from '@/features/auth/schemas/profileSchema'
import { fromApiPhone, toApiPhone } from '@/features/auth/phone'
import type { ProfilePayload, User } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

const t = uk.auth
const p = uk.profile.edit

function toPayload(values: ProfileInput): ProfilePayload {
  const base = { name: values.name, phone: toApiPhone(values.phone), settlementId: values.settlement!.id }
  if (!values.hasOwnerType || values.ownerType === '') return base
  const company = values.ownerType === 'COMPANY' ? { companyName: values.companyName, edrpou: values.edrpou } : {}
  return { ...base, ownerType: values.ownerType, ...company }
}

export function ProfileForm({ user }: { user: User }) {
  const update = useUpdateProfile()
  const form = useForm<ProfileInput>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      name: user.name,
      phone: fromApiPhone(user.phone),
      settlement: toSettlementValue(user.settlement),
      hasOwnerType: user.role === 'USER',
      ownerType: user.ownerType ?? '',
      companyName: user.companyName ?? '',
      edrpou: user.edrpou ?? '',
    },
  })
  const { errors, isDirty } = form.formState
  const ownerType = useWatch({ control: form.control, name: 'ownerType' })

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const updated = await update.mutateAsync(toPayload(values))
      form.reset({ ...values, companyName: updated.companyName ?? '', edrpou: updated.edrpou ?? '' })
    } catch (error) {
      applyServerErrors(error, form.setError)
    }
  })

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-3">
      {update.isError && <Alert tone="error">{problemMessage(update.error)}</Alert>}
      {update.isSuccess && !isDirty && <Alert tone="success">{p.saved}</Alert>}
      <FormField label={t.fields.name} htmlFor="prof-name" error={errors.name?.message}>
        <Input id="prof-name" autoComplete="name" invalid={!!errors.name} {...form.register('name')} />
      </FormField>
      <FormField
        label={t.fields.email}
        htmlFor="prof-email"
        hint={`${p.emailReadonly} · ${user.emailVerified ? p.verified : p.notVerified}`}
      >
        <Input id="prof-email" type="email" value={user.email} disabled readOnly />
      </FormField>
      <FormField label={t.fields.phone} htmlFor="prof-phone" error={errors.phone?.message}>
        <Controller
          control={form.control}
          name="phone"
          render={({ field: { ref, ...phone } }) => <PhoneInput id="prof-phone" ref={ref} invalid={!!errors.phone} {...phone} />}
        />
      </FormField>
      <Controller
        control={form.control}
        name="settlement"
        render={({ field: { value, onChange } }) => (
          <SettlementPicker idPrefix="prof" value={value} onChange={onChange} error={errors.settlement?.message} />
        )}
      />
      {user.role === 'USER' && (
        <OwnerTypeFields register={form.register} errors={errors} ownerType={ownerType} idPrefix="prof" />
      )}
      <div className="flex justify-end">
        <Button type="submit" loading={update.isPending} disabled={!isDirty}>
          {p.submit}
        </Button>
      </div>
    </form>
  )
}
