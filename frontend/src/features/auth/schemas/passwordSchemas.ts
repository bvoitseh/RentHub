import { z } from 'zod'
import { uk } from '@/i18n/uk'

const v = uk.validation

export const forgotPasswordSchema = z.object({
  email: z.email({ error: v.email }),
})

export type ForgotPasswordInput = z.infer<typeof forgotPasswordSchema>

export const resetPasswordSchema = z
  .object({
    password: z.string().min(8, { error: v.passwordMin }).max(72, { error: v.passwordMax }),
    passwordConfirm: z.string(),
  })
  .refine((d) => d.password === d.passwordConfirm, { path: ['passwordConfirm'], error: v.passwordsMismatch })

export type ResetPasswordInput = z.infer<typeof resetPasswordSchema>
