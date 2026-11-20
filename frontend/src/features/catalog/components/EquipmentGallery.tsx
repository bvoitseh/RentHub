import { useState } from 'react'
import { EquipmentCover } from '@/features/catalog/components/EquipmentCover'
import type { Photo } from '@/features/catalog/types'

export interface EquipmentGalleryProps {
  photos: Photo[]
  title: string
}

/** Main photo with clickable thumbnails (equipment card). */
export function EquipmentGallery({ photos, title }: EquipmentGalleryProps) {
  const [index, setIndex] = useState(0)
  const current = photos[Math.min(index, photos.length - 1)]

  return (
    <div className="flex flex-col gap-2">
      <div className="overflow-hidden rounded-lg border border-slate-200">
        <EquipmentCover url={current?.url ?? null} alt={title} />
      </div>
      {photos.length > 1 && (
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
        </div>
      )}
    </div>
  )
}
