import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import type { Video } from '@/features/catalog/types'
import { MediaDropZone } from '@/features/listings/components/MediaDropZone'
import { useVideoMutations } from '@/features/listings/hooks/useListings'
import { VIDEO_MAX_BYTES, VIDEO_TYPES } from '@/features/listings/media'
import { uk } from '@/i18n/uk'

const m = uk.media

/** FR-4a (D-52): one optional video (MP4 / WebM, ≤ 50 MB) with upload progress; a new one replaces the old one. */
export function VideoManager({ listingId, video }: { listingId: number; video: Video | null }) {
  const [percent, setPercent] = useState(0)
  const [local, setLocal] = useState<string | null>(null)
  const { upload, remove } = useVideoMutations(listingId, setPercent)
  const error =
    local ?? (upload.error ? problemMessage(upload.error) : remove.error ? problemMessage(remove.error) : null)

  const choose = (files: File[]) => {
    const file = files[0]
    if (!file) return
    upload.reset()
    if (!VIDEO_TYPES.includes(file.type)) return setLocal(m.videoType)
    if (file.size > VIDEO_MAX_BYTES) return setLocal(m.videoSize)
    setLocal(null)
    setPercent(0)
    upload.mutate(file)
  }

  return (
    <div className="flex flex-col gap-3">
      <h3 className="text-sm font-semibold text-slate-800">{m.video}</h3>
      <p className="text-sm text-slate-600">{m.videoHint}</p>
      {error && <Alert tone="error">{error}</Alert>}
      {video && !upload.isPending && (
        <div className="flex flex-col items-start gap-2">
          <video
            src={video.url}
            controls
            preload="metadata"
            playsInline
            className="max-h-64 w-full rounded-md bg-black"
          />
          <Button variant="secondary" loading={remove.isPending} onClick={() => remove.mutate()}>
            {m.removeVideo}
          </Button>
        </div>
      )}
      {upload.isPending ? (
        <div className="flex flex-col gap-1" aria-live="polite">
          <p className="text-sm text-slate-600">{m.videoUploading(percent)}</p>
          <div className="h-2 overflow-hidden rounded bg-slate-200">
            <div className="h-full bg-brand-600 transition-all" style={{ width: `${percent}%` }} />
          </div>
        </div>
      ) : (
        <MediaDropZone accept={VIDEO_TYPES.join(',')} prompt={m.dropVideo} action={m.chooseVideo} onFiles={choose} />
      )}
    </div>
  )
}
