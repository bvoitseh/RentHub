import { z } from 'zod'
import type { EquipmentDetails } from '@/features/catalog/types'
import type { ListingPayload } from '@/features/listings/types'
import { settlementField } from '@/features/address/schema'
import { toSettlementValue } from '@/features/address/types'
import { uk } from '@/i18n/uk'

const v = uk.validation

const wholeNumber = (min: number, max: number) =>
  z.string().regex(/^\d+$/, { error: v.wholeNumber(min, max) }).refine((s) => Number(s) >= min && Number(s) <= max, {
    error: v.wholeNumber(min, max),
  })

/** FR-4, FR-4b, FR-4c client-side rules; the backend checks the same ones. Numbers are edited as strings. */
export const listingSchema = z
  .object({
    title: z.string().trim().min(1, { error: v.required }).max(120, { error: v.titleMax }),
    description: z.string().trim().min(1, { error: v.required }).max(4000, { error: v.descriptionMax }),
    categoryId: z.string().min(1, { error: v.required }),
    condition: z.enum(['NEW', 'EXCELLENT', 'GOOD', 'FAIR'], { error: v.required }),
    pricePerDay: z.string().regex(/^\d{1,8}([.,]\d{1,2})?$/, { error: v.price }).refine((s) => Number(s.replace(',', '.')) > 0, {
      error: v.price,
    }),
    quantity: wholeNumber(1, 1000),
    minDays: wholeNumber(1, 365),
    maxDays: wholeNumber(1, 365).or(z.literal('')),
    pickupAvailable: z.boolean(),
    deliveryAvailable: z.boolean(),
    deliveryNote: z.string().trim().max(500, { error: v.deliveryNoteMax }),
    address: z.string().trim().min(1, { error: v.required }).max(300, { error: v.addressMax }),
    settlement: settlementField,
    postalCode: z.string().trim().regex(/^(\d{5})?$/, { error: v.postalCode }),
    point: z.object({ lat: z.number(), lon: z.number() }).nullable(),
  })
  .refine((d) => d.maxDays === '' || Number(d.maxDays) >= Number(d.minDays), { path: ['maxDays'], error: v.maxDaysBelowMin })
  .refine((d) => d.pickupAvailable || d.deliveryAvailable, { path: ['deliveryAvailable'], error: v.handoverRequired })
  .refine((d) => d.point !== null, { path: ['point'], error: v.pinRequired })

export type ListingInput = z.infer<typeof listingSchema>

export const emptyListing: ListingInput = {
  title: '',
  description: '',
  categoryId: '',
  condition: 'GOOD',
  pricePerDay: '',
  quantity: '1',
  minDays: '1',
  maxDays: '',
  pickupAvailable: true,
  deliveryAvailable: false,
  deliveryNote: '',
  address: '',
  settlement: null,
  postalCode: '',
  point: null,
}

export function toListingInput(item: EquipmentDetails): ListingInput {
  return {
    title: item.title,
    description: item.description,
    categoryId: String(item.category.id),
    condition: item.condition,
    pricePerDay: String(item.pricePerDay),
    quantity: String(item.quantity),
    minDays: String(item.minDays),
    maxDays: item.maxDays === null ? '' : String(item.maxDays),
    pickupAvailable: item.pickupAvailable,
    deliveryAvailable: item.deliveryAvailable,
    deliveryNote: item.deliveryNote ?? '',
    address: item.address,
    settlement: toSettlementValue(item.settlement),
    postalCode: item.postalCode ?? '',
    point: { lat: item.lat, lon: item.lon },
  }
}

export function toListingPayload(values: ListingInput): ListingPayload {
  const point = values.point ?? { lat: 0, lon: 0 }
  return {
    title: values.title,
    description: values.description,
    categoryId: Number(values.categoryId),
    condition: values.condition,
    pricePerDay: Number(values.pricePerDay.replace(',', '.')),
    quantity: Number(values.quantity),
    minDays: Number(values.minDays),
    maxDays: values.maxDays === '' ? null : Number(values.maxDays),
    pickupAvailable: values.pickupAvailable,
    deliveryAvailable: values.deliveryAvailable,
    deliveryNote: values.deliveryAvailable && values.deliveryNote ? values.deliveryNote : null,
    settlementId: values.settlement!.id,
    address: values.address,
    postalCode: values.postalCode || null,
    lat: point.lat,
    lon: point.lon,
  }
}
