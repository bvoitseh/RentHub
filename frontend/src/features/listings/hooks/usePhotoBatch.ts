import { useState } from 'react'
import { problemMessage } from '@/api/problem'
import type { Photo } from '@/features/catalog/types'
import { usePhotoMutations } from '@/features/listings/hooks/useListings'
import { MAX_PHOTOS, PHOTO_MAX_BYTES, PHOTO_TYPES } from '@/features/listings/media'
import { uk } from '@/i18n/uk'

const t = uk.media

export interface FileError {
  name: string
  reason: string
}

/**
 * FR-4a (D-52): several photos at once. Files are pre-checked (type, 5 MB, free slots up to 10), then uploaded one
 * after another (the backend validates each); errors are collected per file instead of stopping the batch.
 */
export function usePhotoBatch(listingId: number, photos: Photo[]) {
  const { upload, remove } = usePhotoMutations(listingId)
  const [progress, setProgress] = useState<{ done: number; total: number } | null>(null)
  const [errors, setErrors] = useState<FileError[]>([])

  const start = async (files: File[]) => {
    const failed: FileError[] = []
    const accepted: File[] = []
    let free = MAX_PHOTOS - photos.length
    for (const file of files) {
      if (!PHOTO_TYPES.includes(file.type)) failed.push({ name: file.name, reason: t.photoType })
      else if (file.size > PHOTO_MAX_BYTES) failed.push({ name: file.name, reason: t.photoSize })
      else if (free <= 0) failed.push({ name: file.name, reason: t.tooManyPhotos(MAX_PHOTOS) })
      else {
        accepted.push(file)
        free--
      }
    }
    setErrors(failed)
    for (const [i, file] of accepted.entries()) {
      setProgress({ done: i + 1, total: accepted.length })
      try {
        await upload.mutateAsync(file)
      } catch (error) {
        failed.push({ name: file.name, reason: problemMessage(error) })
        setErrors([...failed])
      }
    }
    setProgress(null)
  }

  return { start, progress, errors, remove, full: photos.length >= MAX_PHOTOS }
}
