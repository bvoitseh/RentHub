import { z } from 'zod'
import { EDRPOU_PATTERN, PHONE_PATTERN } from '@/features/auth/schemas/registerSchema'
import { uk } from '@/i18n/uk'

const v = uk.validation

export const profileSchema = z
  .object({
    name: z.string().trim().min(1, { error: v.required }).max(100, { error: v.nameMax }),
    phone: z.string().regex(PHONE_PATTERN, { error: v.phone }),
    city: z.string().trim().min(1, { error: v.required }).max(100, { error: v.cityMax }),
    hasOwnerType: z.boolean(),
    ownerType: z.enum(['PRIVATE', 'COMPANY']).or(z.literal('')),
    companyName: z.string().trim().max(200),
    edrpou: z.string().trim(),
  })
  .refine((d) => !d.hasOwnerType || d.ownerType !== '', { path: ['ownerType'], error: v.ownerTypeRequired })
  .refine((d) => d.ownerType !== 'COMPANY' || d.companyName.length > 0, {
    path: ['companyName'],
    error: v.companyNameRequired,
  })
  .refine((d) => d.ownerType !== 'COMPANY' || EDRPOU_PATTERN.test(d.edrpou), { path: ['edrpou'], error: v.edrpou })

export type ProfileInput = z.infer<typeof profileSchema>
