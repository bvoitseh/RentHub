import type { MouseEvent } from 'react'
import { formatDuration, formatSize } from '@/features/chat/format'
import { useAudioPlayback } from '@/features/chat/hooks/useAudioPlayback'
import { useWaveform, WAVEFORM_BARS } from '@/features/chat/hooks/useWaveform'
import type { ChatAttachment } from '@/features/chat/types'
import { uk } from '@/i18n/uk'

const t = uk.chat.voice
const FLAT = Array.from({ length: WAVEFORM_BARS }, () => 0.15)

/** Colours for the sender's own bubble (accent background) and the other side's (light background). */
const styles = {
  own: { button: 'bg-on-accent text-brand-700', played: 'bg-on-accent', rest: 'bg-on-accent/40' },
  other: { button: 'bg-brand-600 text-on-accent', played: 'bg-brand-600', rest: 'bg-slate-400/60' },
}

/** A voice message: round play button, the recording's waveform (click to seek), time and size. */
export function VoicePlayer({ file, own }: { file: ChatAttachment; own: boolean }) {
  const { waveform, failed } = useWaveform(file.url)
  const { audio, playing, position, toggle, seek } = useAudioPlayback()
  const style = own ? styles.own : styles.other
  const duration = waveform?.duration ?? 0
  const progress = duration > 0 ? Math.min(1, position / duration) : 0
  const peaks = waveform?.peaks ?? FLAT
  const shown = playing || position > 0 ? position : duration

  const onSeek = (event: MouseEvent<HTMLDivElement>) => {
    if (!duration) return
    const box = event.currentTarget.getBoundingClientRect()
    seek(((event.clientX - box.left) / box.width) * duration)
  }

  return (
    <div className="flex w-64 max-w-full items-center gap-3">
      <audio ref={audio} src={file.url} preload="metadata" hidden />
      <button
        type="button"
        onClick={toggle}
        aria-label={playing ? t.pause : t.play}
        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${style.button}`}
      >
        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
          {playing ? <path d="M6 5h4v14H6zM14 5h4v14h-4z" /> : <path d="M7 4.5v15l13-7.5z" />}
        </svg>
      </button>
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <div
          className="flex h-7 cursor-pointer items-center gap-[2px]"
          onClick={onSeek}
          role="slider"
          aria-label={t.seek}
          aria-valuemin={0}
          aria-valuemax={Math.round(duration)}
          aria-valuenow={Math.round(position)}
          tabIndex={-1}
        >
          {peaks.map((peak, i) => (
            <span
              key={i}
              className={`w-[3px] shrink-0 rounded-full ${(i + 0.5) / peaks.length <= progress ? style.played : style.rest}`}
              style={{ height: `${Math.max(12, peak * 100)}%` }}
            />
          ))}
        </div>
        <span className="text-xs opacity-80">
          {failed ? t.failed : `${formatDuration(Math.round(shown))}, ${formatSize(file.size)}`}
        </span>
      </div>
    </div>
  )
}
