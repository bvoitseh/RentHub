import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/features/listings/components/ConfirmDialog'
import { CancelDialog } from '@/features/booking/components/CancelDialog'
import { todayIso } from '@/features/booking/dates'
import { useBookingDecisions } from '@/features/booking/hooks/useBookingMutations'
import type { Booking } from '@/features/booking/types'
import { uk } from '@/i18n/uk'

const t = uk.booking.actions

/**
 * What the caller may do with a booking (FR-12, FR-13, FR-13a): the owner confirms or rejects a NEW request and
 * cancels a CONFIRMED one with a reason; the renter cancels NEW or CONFIRMED. Cancelling only before the start.
 */
export function BookingActions({ booking, as }: { booking: Booking; as: 'owner' | 'renter' }) {
  const { confirm, reject, cancel } = useBookingDecisions()
  const [dialog, setDialog] = useState<'cancel' | 'reject' | null>(null)
  const beforeStart = todayIso() < booking.dateFrom
  const isNew = booking.status === 'NEW'
  const canCancel =
    beforeStart && (as === 'renter' ? isNew || booking.status === 'CONFIRMED' : booking.status === 'CONFIRMED')
  const decisionError = confirm.error ?? reject.error

  if (!canCancel && !(as === 'owner' && isNew)) return null

  return (
    <>
      {as === 'owner' && isNew && (
        <>
          <Button loading={confirm.isPending} onClick={() => confirm.mutate(booking.id)}>
            {t.confirm}
          </Button>
          <Button variant="secondary" onClick={() => setDialog('reject')}>
            {t.reject}
          </Button>
        </>
      )}
      {canCancel && (
        <Button variant="secondary" onClick={() => setDialog('cancel')}>
          {t.cancel}
        </Button>
      )}
      {decisionError && <p className="max-w-56 text-xs text-red-700">{problemMessage(decisionError)}</p>}
      <ConfirmDialog
        open={dialog === 'reject'}
        title={t.rejectTitle}
        text={t.rejectText}
        confirmLabel={t.reject}
        cancelLabel={t.back}
        danger
        loading={reject.isPending}
        onConfirm={() => reject.mutate(booking.id, { onSettled: () => setDialog(null) })}
        onClose={() => setDialog(null)}
      />
      <CancelDialog
        open={dialog === 'cancel'}
        needsReason={as === 'owner'}
        loading={cancel.isPending}
        error={cancel.error}
        onConfirm={(reason) => cancel.mutate({ id: booking.id, reason }, { onSuccess: () => setDialog(null) })}
        onClose={() => {
          cancel.reset()
          setDialog(null)
        }}
      />
    </>
  )
}
