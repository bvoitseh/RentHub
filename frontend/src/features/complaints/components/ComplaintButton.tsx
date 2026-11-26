import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { complaintsApi } from '@/features/complaints/api/complaintsApi'
import { ComplaintDialog } from '@/features/complaints/components/ComplaintDialog'
import type { ComplaintSubject } from '@/features/complaints/types'
import { uk } from '@/i18n/uk'

const t = uk.complaints

/** FR-21: «Поскаржитися» for a signed-in user (not the admin); a verified e-mail is required to send. */
export function ComplaintButton({ subject }: { subject: ComplaintSubject }) {
  const { data: user } = useCurrentUser()
  const [open, setOpen] = useState(false)
  const [version, setVersion] = useState(0)
  const send = useMutation({
    mutationFn: (text: string) => complaintsApi.create(subject, text),
  })

  if (!user || user.role !== 'USER') return null
  const title = 'bookingId' in subject ? t.titleBooking : t.titleListing

  return (
    <div className="flex flex-col items-start gap-2">
      {send.isSuccess ? (
        <Alert tone="success">{t.sent}</Alert>
      ) : (
        <Button
          variant="ghost"
          className="text-red-700"
          onClick={() => {
            send.reset()
            setVersion((v) => v + 1)
            setOpen(true)
          }}
        >
          {t.open}
        </Button>
      )}
      <ComplaintDialog
        key={version}
        open={open}
        title={title}
        notice={user.emailVerified ? undefined : t.verifyFirst}
        loading={send.isPending}
        error={send.error}
        onClose={() => setOpen(false)}
        onSend={(text) => send.mutate(text, { onSuccess: () => setOpen(false) })}
      />
    </div>
  )
}
