import { uk } from '@/i18n/uk'

/** A 1–5 rating as filled and empty stars, with the number for screen readers. */
export function Stars({ value }: { value: number }) {
  return (
    <span className="text-amber-500" role="img" aria-label={uk.reviews.stars(value)}>
      {'★'.repeat(value)}
      <span className="text-slate-300 dark:text-slate-400">{'★'.repeat(5 - value)}</span>
    </span>
  )
}
