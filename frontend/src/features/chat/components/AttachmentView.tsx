import { formatSize } from '@/features/chat/format'
import type { ChatMessage } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

const t = uk.chat

/** An attachment in a bubble: inline image, audio player, video player, or a download link for other files. */
export function AttachmentView({ message }: { message: ChatMessage }) {
  const file = message.attachment
  if (!file) return null

  if (message.kind === 'VOICE') {
    return <audio controls preload="metadata" src={file.url} className="w-64 max-w-full" aria-label={t.kinds.VOICE} />
  }
  if (message.kind === 'VIDEO') {
    return (
      <video controls preload="metadata" src={file.url} className="max-h-80 w-full max-w-sm rounded-md bg-black">
        <a href={file.url}>{t.download}</a>
      </video>
    )
  }
  if (file.contentType.startsWith('image/')) {
    return (
      <a href={file.url} target="_blank" rel="noopener noreferrer">
        <img src={file.url} alt={file.name} loading="lazy" className="max-h-64 max-w-full rounded-md object-contain" />
      </a>
    )
  }
  return (
    <a
      href={file.url}
      target="_blank"
      rel="noopener noreferrer"
      className="flex items-center gap-2 rounded-md border border-current/20 px-3 py-2 underline-offset-2 hover:underline"
    >
      <svg className="h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
        <path d="M14 3H6v18h12V7zM14 3v4h4" />
      </svg>
      <span className="min-w-0 truncate">{file.name}</span>
      <span className="shrink-0 opacity-75">{formatSize(file.size)}</span>
    </a>
  )
}
