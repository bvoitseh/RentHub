import { useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { IconButton } from '@/components/ui/IconButton'
import { PaperclipIcon } from '@/features/chat/components/ChatIcons'
import { VoiceRecorder } from '@/features/chat/components/VoiceRecorder'
import { useSendAttachment, useSendText } from '@/features/chat/hooks/useChatMutations'
import { useVoiceRecorder } from '@/features/chat/hooks/useVoiceRecorder'
import { uk } from '@/i18n/uk'

const t = uk.chat.composer
const ACCEPT =
  '.jpg,.jpeg,.png,.webp,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.odt,.ods,.odp,.txt,.zip,.mp4,.webm,video/mp4,video/webm'
const MAX_LENGTH = 4000
const micErrors = { denied: t.micDenied, notFound: t.micNotFound, unsupported: t.micUnsupported } as const

/** Paperclip (a file or a video), text box (Enter sends, Shift+Enter breaks the line), microphone and send. */
export function MessageComposer({ conversationId }: { conversationId: number }) {
  const [text, setText] = useState('')
  const sendText = useSendText(conversationId)
  const sendFile = useSendAttachment(conversationId)
  const fileInput = useRef<HTMLInputElement>(null)
  const sendVoice = (file: File) => {
    sendText.reset()
    sendFile.mutate({ kind: 'VOICE', file })
  }
  const voice = useVoiceRecorder(sendVoice)
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
  const attach = (file: File | undefined) => {
    if (!file) return
    sendText.reset()
    // MP4 / WebM files are sent as videos, everything else as a file.
    const video = file.type.startsWith('video/') || /\.(mp4|webm)$/i.test(file.name)
    sendFile.mutate({ kind: video ? 'VIDEO' : 'FILE', file })
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-2 border-t border-slate-200 p-3">
      {error && <Alert tone="error">{problemMessage(error)}</Alert>}
      {sendFile.progress !== null && <p className="text-sm text-slate-600">{t.uploading(sendFile.progress)}</p>}
      <div className="flex items-end gap-2">
        {!voice.recording && (
          <>
            <IconButton label={t.attach} disabled={busy} onClick={() => fileInput.current?.click()}>
              <PaperclipIcon />
            </IconButton>
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
          </>
        )}
        <VoiceRecorder voice={voice} disabled={busy} onRecorded={sendVoice} />
        {!voice.recording && (
          <Button type="submit" loading={sendText.isPending} disabled={!text.trim() || sendFile.isPending}>
            {t.send}
          </Button>
        )}
      </div>
      {voice.error && <p className="text-xs text-red-700">{micErrors[voice.error]}</p>}
      <input
        ref={fileInput}
        type="file"
        accept={ACCEPT}
        hidden
        onChange={(e) => {
          attach(e.target.files?.[0])
          e.target.value = ''
        }}
      />
    </form>
  )
}
