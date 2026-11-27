import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import type { Photo } from '@/features/catalog/types'
import { MediaDropZone } from '@/features/listings/components/MediaDropZone'
import { usePhotoBatch } from '@/features/listings/hooks/usePhotoBatch'
import { PHOTO_TYPES } from '@/features/listings/media'
import { uk } from '@/i18n/uk'

const t = uk.listings.photos
const m = uk.media

export interface PhotoManagerProps {
  listingId: number
  photos: Photo[]
}

/** FR-4a (D-52): several photos at once (choose or drop), up to 10, remove; the first one is the cover. */
export function PhotoManager({ listingId, photos }: PhotoManagerProps) {
  const { start, progress, errors, remove, full } = usePhotoBatch(listingId, photos)

  return (
    <div className="flex flex-col gap-3">
      <p className="text-sm text-slate-600">{m.photosHint}</p>
      {remove.error && <Alert tone="error">{problemMessage(remove.error)}</Alert>}
      {errors.length > 0 && (
        <Alert tone="error">
          {m.notUploaded}
          <ul className="mt-1 list-disc pl-5">
            {errors.map((e) => (
              <li key={e.name + e.reason}>
                {e.name} — {e.reason}
              </li>
            ))}
          </ul>
        </Alert>
      )}
      <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
        {photos.map((photo) => (
          <li key={photo.id} className="relative overflow-hidden rounded-md border border-slate-200">
            <img src={photo.url} alt="" className="aspect-square w-full object-cover" />
            {photo.position === 0 && (
              <span className="absolute left-1 top-1 rounded bg-brand-600 px-1.5 py-0.5 text-xs text-on-accent">
                {t.cover}
              </span>
            )}
            <button
              type="button"
              onClick={() => remove.mutate(photo.id)}
              disabled={remove.isPending || progress !== null}
              aria-label={t.remove}
              className="absolute right-1 top-1 rounded-full bg-surface/90 px-2 text-sm text-red-700 shadow hover:bg-surface"
            >
              ×
            </button>
          </li>
        ))}
      </ul>
      {progress ? (
        <p className="text-sm text-slate-600" aria-live="polite">
          {m.uploadingOf(progress.done, progress.total)}
        </p>
      ) : full ? (
        <p className="text-sm text-slate-600">{t.limit}</p>
      ) : (
        <MediaDropZone
          accept={PHOTO_TYPES.join(',')}
          multiple
          prompt={m.dropPhotos}
          action={m.choosePhotos}
          onFiles={start}
        />
      )}
    </div>
  )
}
