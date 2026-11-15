import type { FieldErrors, FieldValues, Path, UseFormRegister } from 'react-hook-form'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import type { OwnerType } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

export interface OwnerFieldValues extends FieldValues {
  ownerType: OwnerType | ''
  companyName: string
  edrpou: string
}

export interface OwnerTypeFieldsProps<T extends OwnerFieldValues> {
  register: UseFormRegister<T>
  errors: FieldErrors<T>
  ownerType: OwnerType | ''
  idPrefix: string
}

const t = uk.auth

/** FR-1a owner-type section shared by registration and profile forms; company fields appear only for COMPANY. */
export function OwnerTypeFields<T extends OwnerFieldValues>({ register, errors, ownerType, idPrefix }: OwnerTypeFieldsProps<T>) {
  const message = (name: keyof OwnerFieldValues) => errors[name as Path<T>]?.message as string | undefined
  const options = [
    { value: 'PRIVATE', label: t.ownerTypes.PRIVATE },
    { value: 'COMPANY', label: t.ownerTypes.COMPANY },
  ]

  return (
    <>
      <FormField label={t.fields.ownerType} htmlFor={`${idPrefix}-ownerType`} error={message('ownerType')}>
        <Select
          id={`${idPrefix}-ownerType`}
          options={options}
          placeholder="—"
          invalid={!!message('ownerType')}
          {...register('ownerType' as Path<T>)}
        />
      </FormField>
      {ownerType === 'COMPANY' && (
        <>
          <FormField label={t.fields.companyName} htmlFor={`${idPrefix}-companyName`} error={message('companyName')}>
            <Input
              id={`${idPrefix}-companyName`}
              autoComplete="organization"
              invalid={!!message('companyName')}
              {...register('companyName' as Path<T>)}
            />
          </FormField>
          <FormField label={t.fields.edrpou} htmlFor={`${idPrefix}-edrpou`} error={message('edrpou')}>
            <Input
              id={`${idPrefix}-edrpou`}
              inputMode="numeric"
              maxLength={8}
              invalid={!!message('edrpou')}
              {...register('edrpou' as Path<T>)}
            />
          </FormField>
        </>
      )}
    </>
  )
}
