import { useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { VoiceRecorder } from '@/features/chat/components/VoiceRecorder'
import { useSendAttachment, useSendText } from '@/features/chat/hooks/useChatMutations'
import type { AttachmentKind } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

const t = uk.chat.composer
const FILE_TYPES = '.jpg,.jpeg,.png,.webp,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.odt,.ods,.odp,.txt,.zip'
const MAX_LENGTH = 4000

/** Text box (Enter sends, Shift+Enter breaks the line) plus file, video and voice buttons. */
export function MessageComposer({ conversationId }: { conversationId: number }) {
  const [text, setText] = useState('')
  const sendText = useSendText(conversationId)
  const sendFile = useSendAttachment(conversationId)
  const fileInput = useRef<HTMLInputElement>(null)
  const videoInput = useRef<HTMLInputElement>(null)
  const busy = sendText.isPending || sendFile.isPending
  const error = sendText.error ?? sendFile.error

  const submit = (event?: FormEvent) => {
    event?.preventDefault()
    const body = text.trim()
    if (!body || busy) return
    sendText.mutate(body, { onSuccess: () => setText('') })
  }
  const onKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) submit(event)
  }
  const upload = (kind: AttachmentKind, file: File | undefined) => {
    if (!file) return
    sendText.reset()
    sendFile.mutate({ kind, file })
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-2 border-t border-slate-200 p-3">
      {error && <Alert tone="error">{problemMessage(error)}</Alert>}
      {sendFile.progress !== null && <p className="text-sm text-slate-600">{t.uploading(sendFile.progress)}</p>}
      <div className="flex items-end gap-2">
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={onKeyDown}
          rows={2}
          maxLength={MAX_LENGTH}
          placeholder={t.placeholder}
          aria-label={t.placeholder}
          className="min-h-[2.75rem] flex-1 resize-y rounded-md border border-slate-300 bg-surface px-3 py-2 text-sm text-slate-900 focus:border-brand-600 focus:outline-none"
        />
        <Button type="submit" loading={sendText.isPending} disabled={!text.trim() || sendFile.isPending}>
          {t.send}
        </Button>
      </div>
      <div className="flex flex-wrap items-start gap-2">
        <Button variant="secondary" disabled={busy} title={t.attachHint} onClick={() => fileInput.current?.click()}>
          {t.attach}
        </Button>
        <Button variant="secondary" disabled={busy} title={t.videoHint} onClick={() => videoInput.current?.click()}>
          {t.video}
        </Button>
        <VoiceRecorder disabled={busy} onRecorded={(file) => upload('VOICE', file)} />
        <input
          ref={fileInput}
          type="file"
          accept={FILE_TYPES}
          hidden
          onChange={(e) => {
            upload('FILE', e.target.files?.[0])
            e.target.value = ''
          }}
        />
        <input
          ref={videoInput}
          type="file"
          accept="video/mp4,video/webm"
          hidden
          onChange={(e) => {
            upload('VIDEO', e.target.files?.[0])
            e.target.value = ''
          }}
        />
      </div>
    </form>
  )
}
