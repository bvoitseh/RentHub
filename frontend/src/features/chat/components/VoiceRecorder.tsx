import { Button } from '@/components/ui/Button'
import { formatDuration } from '@/features/chat/format'
import { useVoiceRecorder } from '@/features/chat/hooks/useVoiceRecorder'
import { uk } from '@/i18n/uk'

const t = uk.chat.composer

export interface VoiceRecorderProps {
  disabled: boolean
  onRecorded: (file: File) => void
}

/** «Голосове» → recording with a timer → «Зупинити й надіслати» or «Скасувати»; stops by itself at 5 minutes. */
export function VoiceRecorder({ disabled, onRecorded }: VoiceRecorderProps) {
  const voice = useVoiceRecorder(onRecorded)

  if (voice.recording) {
    return (
      <div className="flex flex-wrap items-center gap-2" role="status">
        <span className="flex items-center gap-2 text-sm text-red-700">
          <span className="h-2.5 w-2.5 animate-pulse rounded-full bg-red-600" aria-hidden />
          {t.recording(formatDuration(voice.seconds))}
        </span>
        <Button
          onClick={() =>
            void voice.stop().then((file) => {
              if (file) onRecorded(file)
            })
          }
        >
          {t.stop}
        </Button>
        <Button variant="ghost" onClick={voice.cancel}>
          {t.cancel}
        </Button>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-start gap-1">
      <Button variant="secondary" disabled={disabled} onClick={() => void voice.start()}>
        {t.record}
      </Button>
      {voice.error && (
        <p className="text-xs text-red-700">{voice.error === 'denied' ? t.micDenied : t.micUnsupported}</p>
      )}
    </div>
  )
}
