import { useState } from 'react'
import { EquipmentCover } from '@/features/catalog/components/EquipmentCover'
import type { Photo, Video } from '@/features/catalog/types'
import { uk } from '@/i18n/uk'

export interface EquipmentGalleryProps {
  photos: Photo[]
  video?: Video | null
  title: string
}

/** Main photo or the video (FR-4a, D-52) with clickable thumbnails; the video is the last thumbnail. */
export function EquipmentGallery({ photos, video = null, title }: EquipmentGalleryProps) {
  const [index, setIndex] = useState(0)
  const count = photos.length + (video ? 1 : 0)
  const showVideo = video !== null && index === photos.length
  const current = photos[Math.min(index, photos.length - 1)]

  return (
    <div className="flex flex-col gap-2">
      <div className="overflow-hidden rounded-lg border border-slate-200">
        {showVideo ? (
          <video
            src={video.url}
            controls
            preload="metadata"
            playsInline
            className="aspect-[4/3] w-full bg-black object-contain"
          />
        ) : (
          <EquipmentCover url={current?.url ?? null} alt={title} />
        )}
      </div>
      {count > 1 && (
        <div className="flex gap-2 overflow-x-auto">
          {photos.map((photo, i) => (
            <button
              key={photo.id}
              type="button"
              onClick={() => setIndex(i)}
              aria-label={`${title} — ${i + 1}`}
              aria-current={i === index}
              className={`h-16 w-20 shrink-0 overflow-hidden rounded-md border-2 ${
                i === index ? 'border-brand-600' : 'border-transparent opacity-70 hover:opacity-100'
              }`}
            >
              <img src={photo.url} alt="" className="h-full w-full object-cover" />
            </button>
          ))}
          {video && (
            <button
              type="button"
              onClick={() => setIndex(photos.length)}
              aria-label={`${title} — ${uk.media.playVideo}`}
              aria-current={showVideo}
              className={`flex h-16 w-20 shrink-0 items-center justify-center rounded-md border-2 bg-slate-950 text-white ${
                showVideo ? 'border-brand-600' : 'border-transparent opacity-70 hover:opacity-100'
              }`}
            >
              <svg className="h-7 w-7" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M8 5v14l11-7z" />
              </svg>
            </button>
          )}
        </div>
      )}
    </div>
  )
}
