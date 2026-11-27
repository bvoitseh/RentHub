import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import { searchKey } from '@/components/ui/normalize'
import { uk } from '@/i18n/uk'

export interface MenuOption {
  value: string
  label: string
}

export interface SelectMenuProps {
  id: string
  options: MenuOption[]
  selected: string
  initialQuery: string
  /** Opens above the trigger when there is no room below. */
  upward: boolean
  onPick: (value: string) => void
  onClose: (returnFocus: boolean) => void
}

/** The opened dropdown (D-51): a search field (case- and apostrophe-insensitive "contains") and the option list. */
export function SelectMenu({ id, options, selected, initialQuery, upward, onPick, onClose }: SelectMenuProps) {
  const [query, setQuery] = useState(initialQuery)
  const filtered = useMemo(() => {
    const key = searchKey(query)
    return key ? options.filter((o) => searchKey(o.label).includes(key)) : options
  }, [options, query])
  const [active, setActive] = useState(() => Math.max(0, options.findIndex((o) => o.value === selected)))
  const list = useRef<HTMLUListElement>(null)
  const index = Math.min(active, Math.max(filtered.length - 1, 0))

  useEffect(() => {
    list.current?.querySelector<HTMLElement>(`[data-index="${index}"]`)?.scrollIntoView({ block: 'nearest' })
  }, [index])

  const onKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    const last = filtered.length - 1
    const keys: Record<string, () => void> = {
      ArrowDown: () => setActive(Math.min(index + 1, last)),
      ArrowUp: () => setActive(Math.max(index - 1, 0)),
      Home: () => setActive(0),
      End: () => setActive(Math.max(last, 0)),
      Enter: () => filtered[index] && onPick(filtered[index].value),
      Escape: () => onClose(true),
    }
    const handler = keys[event.key]
    if (!handler) return
    event.preventDefault()
    event.stopPropagation() // Escape must not also close a surrounding <dialog>
    handler()
  }

  return (
    <div
      className={`absolute left-0 z-30 w-full min-w-48 rounded-md border border-slate-200 bg-surface p-1 shadow-lg ${
        upward ? 'bottom-full mb-1' : 'top-full mt-1'
      }`}
    >
      <input
        autoFocus
        type="search"
        value={query}
        placeholder={uk.common.search}
        aria-label={uk.common.search}
        aria-controls={id}
        aria-activedescendant={filtered[index] ? `${id}-${index}` : undefined}
        onChange={(event) => {
          setQuery(event.target.value)
          setActive(0)
        }}
        onKeyDown={onKeyDown}
        className="mb-1 block w-full rounded border border-slate-300 bg-surface px-2 py-1.5 text-sm text-slate-900 placeholder:text-slate-500 focus:outline-2 focus:outline-brand-600"
      />
      <ul ref={list} id={id} role="listbox" className="max-h-60 overflow-auto text-sm">
        {filtered.length === 0 && <li className="px-2 py-2 text-slate-500">{uk.common.nothingFound}</li>}
        {filtered.map((option, i) => (
          <li
            key={option.value}
            id={`${id}-${i}`}
            data-index={i}
            role="option"
            aria-selected={option.value === selected}
            onMouseDown={(event) => event.preventDefault()}
            onMouseEnter={() => setActive(i)}
            onClick={() => onPick(option.value)}
            className={`cursor-pointer rounded px-2 py-1.5 ${i === index ? 'bg-brand-50 text-brand-800' : 'text-slate-800'} ${
              option.value === selected ? 'font-semibold' : ''
            }`}
          >
            {option.label}
          </li>
        ))}
      </ul>
    </div>
  )
}
