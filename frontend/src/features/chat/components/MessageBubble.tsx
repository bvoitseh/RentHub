import { AttachmentView } from '@/features/chat/components/AttachmentView'
import { formatMessageTime } from '@/features/chat/format'
import type { ChatMessage } from '@/features/chat/types'

/** One message: own messages on the right in the accent colour, the other side's on the left. */
export function MessageBubble({ message, own }: { message: ChatMessage; own: boolean }) {
  return (
    <li className={`flex ${own ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`flex max-w-[85%] flex-col gap-1 rounded-lg px-3 py-2 text-sm sm:max-w-[70%] ${
          own ? 'bg-brand-600 text-on-accent' : 'bg-slate-100 text-slate-900'
        }`}
      >
        {message.body && <p className="whitespace-pre-wrap break-words">{message.body}</p>}
        <AttachmentView message={message} own={own} />
        <time dateTime={message.createdAt} className="self-end text-xs opacity-75">
          {formatMessageTime(message.createdAt)}
        </time>
      </div>
    </li>
  )
}
