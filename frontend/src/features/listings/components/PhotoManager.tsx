import { useRef } from 'react'
import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import type { Photo } from '@/features/catalog/types'
import { usePhotoMutations } from '@/features/listings/hooks/useListings'
import { uk } from '@/i18n/uk'

const t = uk.listings.photos
const MAX_PHOTOS = 5

export interface PhotoManagerProps {
  listingId: number
  photos: Photo[]
}

/** FR-4a: upload (JPEG/PNG/WebP, ≤ 5 MB, up to 5) and remove photos; the first one is the cover. */
export function PhotoManager({ listingId, photos }: PhotoManagerProps) {
  const input = useRef<HTMLInputElement>(null)
  const { upload, remove } = usePhotoMutations(listingId)
  const error = upload.error ?? remove.error
  const full = photos.length >= MAX_PHOTOS

  return (
    <div className="flex flex-col gap-3">
      <p className="text-sm text-slate-600">{t.hint}</p>
      {error && <Alert tone="error">{problemMessage(error)}</Alert>}
      <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
        {photos.map((photo) => (
          <li key={photo.id} className="relative overflow-hidden rounded-md border border-slate-200">
            <img src={photo.url} alt="" className="aspect-square w-full object-cover" />
            {photo.position === 0 && (
              <span className="absolute left-1 top-1 rounded bg-brand-600 px-1.5 py-0.5 text-xs text-on-accent">{t.cover}</span>
            )}
            <button
              type="button"
              onClick={() => remove.mutate(photo.id)}
              disabled={remove.isPending}
              aria-label={t.remove}
              className="absolute right-1 top-1 rounded-full bg-surface/90 px-2 text-sm text-red-700 shadow hover:bg-surface"
            >
              ×
            </button>
          </li>
        ))}
      </ul>
      <input
        ref={input}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onChange={(event) => {
          const file = event.target.files?.[0]
          if (file) upload.mutate(file)
          event.target.value = ''
        }}
      />
      <Button
        variant="secondary"
        className="self-start"
        disabled={full}
        loading={upload.isPending}
        onClick={() => input.current?.click()}
      >
        {upload.isPending ? t.uploading : full ? t.limit : t.add}
      </Button>
    </div>
  )
}
