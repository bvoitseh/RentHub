import { z } from 'zod'
import { uk } from '@/i18n/uk'

export const deactivateSchema = z.object({
  password: z.string().min(1, { error: uk.validation.required }),
})

export type DeactivateInput = z.infer<typeof deactivateSchema>
