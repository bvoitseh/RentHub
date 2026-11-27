import type { Settlement } from '@/features/address/types'
import { uk } from '@/i18n/uk'

export interface SettlementSuggestionsProps {
  id: string
  items: Settlement[]
  loading: boolean
  active: number
  onPick: (settlement: Settlement) => void
}

/** The list under the settlement field; labels tell namesakes apart (D-48). */
export function SettlementSuggestions({ id, items, loading, active, onPick }: SettlementSuggestionsProps) {
  const t = uk.address
  return (
    <ul
      id={id}
      role="listbox"
      className="absolute z-30 mt-1 max-h-72 w-full overflow-auto rounded-md border border-slate-200 bg-surface py-1 text-sm shadow-lg"
    >
      {items.length === 0 && <li className="px-3 py-2 text-slate-500">{loading ? t.searching : t.nothingFound}</li>}
      {items.map((s, index) => (
        <li
          key={s.id}
          role="option"
          aria-selected={index === active}
          className={`cursor-pointer px-3 py-2 ${index === active ? 'bg-brand-50 text-brand-800' : 'hover:bg-slate-50'}`}
          onMouseDown={(event) => {
            event.preventDefault()
            onPick(s)
          }}
        >
          {s.label}
        </li>
      ))}
    </ul>
  )
}
