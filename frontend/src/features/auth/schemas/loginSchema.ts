import { z } from 'zod'
import { uk } from '@/i18n/uk'

const v = uk.validation

export const loginSchema = z.object({
  email: z.email({ error: v.email }),
  password: z.string().min(1, { error: v.required }),
})

export type LoginInput = z.infer<typeof loginSchema>
