import { z } from 'zod'
import { uk } from '@/i18n/uk'

const v = uk.validation

export const PHONE_PATTERN = /^\+380\d{9}$/

/** Input attributes for phone fields: the same +380XXXXXXXXX format the schema enforces, plus a phone keyboard. */
export const PHONE_INPUT_PROPS = {
  type: 'tel',
  inputMode: 'tel',
  autoComplete: 'tel',
  placeholder: '+380XXXXXXXXX',
  pattern: PHONE_PATTERN.source.slice(1, -1), // the same regex without ^ and $ (HTML patterns are anchored)
  maxLength: 13,
  title: v.phone,
} as const
export const EDRPOU_PATTERN = /^\d{8}$/

export const registerSchema = z
  .object({
    name: z.string().trim().min(1, { error: v.required }).max(100, { error: v.nameMax }),
    email: z.email({ error: v.email }),
    phone: z.string().regex(PHONE_PATTERN, { error: v.phone }),
    password: z.string().min(8, { error: v.passwordMin }).max(72, { error: v.passwordMax }),
    passwordConfirm: z.string(),
    city: z.string().trim().min(1, { error: v.required }).max(100, { error: v.cityMax }),
    ownerType: z.enum(['PRIVATE', 'COMPANY']),
    companyName: z.string().trim().max(200),
    edrpou: z.string().trim(),
  })
  .refine((d) => d.password === d.passwordConfirm, { path: ['passwordConfirm'], error: v.passwordsMismatch })
  .refine((d) => d.ownerType !== 'COMPANY' || d.companyName.length > 0, {
    path: ['companyName'],
    error: v.companyNameRequired,
  })
  .refine((d) => d.ownerType !== 'COMPANY' || EDRPOU_PATTERN.test(d.edrpou), { path: ['edrpou'], error: v.edrpou })

export type RegisterInput = z.infer<typeof registerSchema>

export const registerDefaults: RegisterInput = {
  name: '',
  email: '',
  phone: '+380',
  password: '',
  passwordConfirm: '',
  city: 'Львів',
  ownerType: 'PRIVATE',
  companyName: '',
  edrpou: '',
}
