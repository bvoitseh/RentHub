import { uk } from '@/i18n/uk'

export interface EquipmentCoverProps {
  url: string | null
  alt: string
  className?: string
}

/** Cover photo with a neutral placeholder when the listing has no photo yet. */
export function EquipmentCover({ url, alt, className = 'aspect-[4/3]' }: EquipmentCoverProps) {
  if (!url) {
    return (
      <div className={`flex items-center justify-center bg-slate-100 text-sm text-slate-400 ${className}`}>
        {uk.catalog.noPhoto}
      </div>
    )
  }
  return <img src={url} alt={alt} loading="lazy" className={`w-full bg-slate-100 object-cover ${className}`} />
}
