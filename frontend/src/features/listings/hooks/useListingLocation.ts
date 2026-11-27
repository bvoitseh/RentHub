import { useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import type { UseFormReturn } from 'react-hook-form'
import type { LatLon } from '@/components/map/leaflet'
import { listingsApi } from '@/features/listings/api/listingsApi'
import type { ListingInput } from '@/features/listings/schemas/listingSchema'
import type { ListingLocation, PinAddress } from '@/features/listings/types'
import { uk } from '@/i18n/uk'

const t = uk.listings.location
const dirty = { shouldDirty: true, shouldValidate: true } as const

/**
 * FR-4 (D-48, D-49): «Знайти на мапі» turns the address into a pin; moving the pin fills the street and house back in.
 * The postal code replaces an empty field or our own earlier suggestion, never a code the owner typed.
 */
export function useListingLocation(form: UseFormReturn<ListingInput>) {
  const suggested = useRef<string | null>(null)
  const latestPin = useRef(0)
  const [last, setLast] = useState<'locate' | 'describe' | null>(null)

  const suggestPostalCode = (code: string | null) => {
    const current = form.getValues('postalCode')
    if (code && (!current || current === suggested.current)) {
      form.setValue('postalCode', code, dirty)
      suggested.current = code
    }
  }

  const locate = useMutation({
    mutationFn: ({ settlementId, street }: { settlementId: number; street: string }) =>
      listingsApi.locate(settlementId, street),
    onSuccess: (result) => {
      form.setValue('point', { lat: result.lat, lon: result.lon }, dirty)
      suggestPostalCode(result.postalCode)
    },
  })

  const describe = useMutation({
    mutationFn: ({ settlementId, point }: { settlementId: number; point: LatLon; seq: number }) =>
      listingsApi.describe(settlementId, point.lat, point.lon),
    onSuccess: (result, { seq }) => {
      if (seq !== latestPin.current) return // a later drag is already on its way
      if (result.address) form.setValue('address', result.address, dirty)
      suggestPostalCode(result.postalCode)
    },
  })

  const find = () => {
    const settlement = form.getValues('settlement')
    if (!settlement) return void form.trigger('settlement')
    setLast('locate')
    locate.mutate({ settlementId: settlement.id, street: form.getValues('address').trim() })
  }

  /** The owner placed or moved the pin by hand. */
  const pinMoved = (point: LatLon) => {
    const settlement = form.getValues('settlement')
    if (!settlement) return
    latestPin.current += 1
    setLast('describe')
    describe.mutate({ settlementId: settlement.id, point, seq: latestPin.current })
  }

  return { find, pinMoved, finding: locate.isPending, ...status(last, locate, describe) }
}

interface Lookup<T> {
  isPending: boolean
  isSuccess: boolean
  isError: boolean
  data?: T
}

function status(
  last: 'locate' | 'describe' | null,
  locate: Lookup<ListingLocation>,
  describe: Lookup<PinAddress>,
): { message: string; warning: boolean } {
  if (last === 'describe') {
    if (describe.isPending) return { message: t.describing, warning: false }
    const found = !describe.isError && !!describe.data?.address
    return { message: found ? t.described : t.notDescribed, warning: !found }
  }
  if (locate.isPending) return { message: t.finding, warning: false }
  if (locate.isError) return { message: t.notFound, warning: true }
  if (locate.isSuccess && locate.data) return locate.data.exact
    ? { message: t.found, warning: false }
    : { message: t.approximate, warning: true }
  return { message: t.hint, warning: false }
}
