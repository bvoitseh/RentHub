import { z } from 'zod'
import { uk } from '@/i18n/uk'

/** A settlement picked from the suggestions (D-48); free text is not accepted. */
export const settlementField = z
  .object({ id: z.number(), label: z.string(), regionId: z.number() })
  .nullable()
  .refine((value) => Boolean(value), { error: uk.address.required })
