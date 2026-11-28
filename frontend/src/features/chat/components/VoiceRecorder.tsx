import { IconButton } from '@/components/ui/IconButton'
import { CloseIcon, MicIcon, SendIcon } from '@/features/chat/components/ChatIcons'
import { formatDuration } from '@/features/chat/format'
import type { useVoiceRecorder } from '@/features/chat/hooks/useVoiceRecorder'
import { uk } from '@/i18n/uk'

const t = uk.chat.composer

export interface VoiceRecorderProps {
  voice: ReturnType<typeof useVoiceRecorder>
  disabled: boolean
  onRecorded: (file: File) => void
}

/** Microphone icon; while recording, the timer with a cancel icon and a stop-and-send icon (5 minutes at most). */
export function VoiceRecorder({ voice, disabled, onRecorded }: VoiceRecorderProps) {
  if (!voice.recording) {
    return (
      <IconButton label={t.record} disabled={disabled} onClick={() => void voice.start()}>
        <MicIcon />
      </IconButton>
    )
  }

  return (
    <div className="flex flex-1 items-center gap-1" role="status">
      <span className="flex flex-1 items-center gap-2 px-2 text-sm text-red-700">
        <span className="h-2.5 w-2.5 animate-pulse rounded-full bg-red-600" aria-hidden />
        {t.recording(formatDuration(voice.seconds))}
      </span>
      <IconButton label={t.cancel} tone="danger" onClick={voice.cancel}>
        <CloseIcon />
      </IconButton>
      <IconButton
        label={t.stop}
        tone="primary"
        onClick={() =>
          void voice.stop().then((file) => {
            if (file) onRecorded(file)
          })
        }
      >
        <SendIcon />
      </IconButton>
    </div>
  )
}
