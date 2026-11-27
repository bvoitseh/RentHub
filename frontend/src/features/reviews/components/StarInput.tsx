import { uk } from '@/i18n/uk'

export interface StarInputProps {
  /** Radio-group name, unique per form (several forms can be on one page). */
  name: string
  value: number
  onChange: (value: number) => void
}

/** Rating picker 1–5 as a radio group of stars (keyboard: Tab to the group, arrows/space via the radios). */
export function StarInput({ name, value, onChange }: StarInputProps) {
  return (
    <fieldset className="flex items-center gap-1">
      <legend className="mb-1 text-sm font-medium text-slate-800">{uk.reviews.form.rating}</legend>
      {[1, 2, 3, 4, 5].map((n) => (
        <label key={n} className="cursor-pointer text-2xl leading-none" title={uk.reviews.stars(n)}>
          <input
            type="radio"
            name={name}
            value={n}
            checked={value === n}
            onChange={() => onChange(n)}
            className="sr-only"
            aria-label={uk.reviews.stars(n)}
          />
          <span className={n <= value ? 'text-amber-500' : 'text-slate-300 hover:text-amber-300 dark:text-slate-400'}>★</span>
        </label>
      ))}
    </fieldset>
  )
}
