import { useEffect, useId, useImperativeHandle, useRef, useState, type Ref, type SelectHTMLAttributes } from 'react'
import { SelectMenu } from '@/components/ui/SelectMenu'
import { useSelectValue } from '@/components/ui/useSelectValue'

export interface SelectOption {
  value: string
  label: string
}

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  options: SelectOption[]
  invalid?: boolean
  placeholder?: string
  /** React 19 passes ref as a prop; react-hook-form uses it to read the value and to focus the field. */
  ref?: Ref<HTMLSelectElement | null>
}

const base =
  'flex w-full items-center justify-between gap-2 rounded-md border bg-surface px-3 py-2 text-left text-sm ' +
  'text-slate-900 shadow-xs focus:outline-2 focus:outline-offset-0 disabled:bg-slate-100 disabled:text-slate-500'

/**
 * Searchable dropdown (D-51) with the props of a native select, so every call site — controlled, react-hook-form
 * `register` or an uncontrolled form read via FormData — works unchanged: a hidden native select keeps the value and
 * receives the events; the visible trigger and the menu only display and pick.
 */
export function Select(props: SelectProps) {
  const { options, invalid = false, placeholder, className = '', id, disabled, ref, title, ...native } = props
  const { 'aria-label': ariaLabel, 'aria-labelledby': labelledBy, ...field } = native
  const generated = useId()
  const listId = `${id ?? generated}-list`
  const select = useRef<HTMLSelectElement>(null)
  const trigger = useRef<HTMLButtonElement>(null)
  const wrapper = useRef<HTMLDivElement>(null)
  const { value, pick } = useSelectValue(select, field.value, options)
  const [menu, setMenu] = useState<{ query: string; upward: boolean } | null>(null)

  // react-hook-form focuses invalid fields through the ref: send that focus to the visible trigger.
  useImperativeHandle(ref, () => {
    const el = select.current as HTMLSelectElement
    el.focus = () => trigger.current?.focus()
    return el
  }, [])

  useEffect(() => {
    if (!menu) return
    const outside = (event: MouseEvent) => {
      if (!wrapper.current?.contains(event.target as Node)) setMenu(null)
    }
    document.addEventListener('mousedown', outside)
    return () => document.removeEventListener('mousedown', outside)
  }, [menu])

  const all = placeholder !== undefined ? [{ value: '', label: placeholder }, ...options] : options
  const label = all.find((o) => o.value === value)?.label ?? placeholder ?? ''
  const open = (query = '') => {
    const rect = trigger.current?.getBoundingClientRect()
    const below = rect ? window.innerHeight - rect.bottom : 999
    setMenu({ query, upward: below < 300 && (rect?.top ?? 0) > below })
  }
  const close = (returnFocus: boolean) => {
    setMenu(null)
    if (returnFocus) trigger.current?.focus()
  }
  const border = invalid ? 'border-red-400 focus:outline-red-500' : 'border-slate-300 focus:outline-brand-600'

  return (
    <div
      ref={wrapper}
      className={`relative ${className}`}
      onBlur={(event) => {
        if (menu && !wrapper.current?.contains(event.relatedTarget as Node)) setMenu(null)
      }}
    >
      <select ref={select} aria-hidden="true" tabIndex={-1} disabled={disabled} className="sr-only" {...field}>
        {all.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
      <button
        ref={trigger}
        id={id}
        type="button"
        role="combobox"
        title={title}
        aria-label={ariaLabel}
        aria-labelledby={labelledBy}
        aria-haspopup="listbox"
        aria-expanded={menu !== null}
        aria-controls={listId}
        aria-invalid={invalid || undefined}
        disabled={disabled}
        className={`${base} ${border}`}
        onClick={() => (menu ? setMenu(null) : open())}
        onKeyDown={(event) => {
          if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(event.key)) {
            event.preventDefault()
            open()
          } else if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey) {
            event.preventDefault()
            open(event.key)
          }
        }}
      >
        <span className={`truncate ${value === '' && placeholder !== undefined ? 'text-slate-500' : ''}`}>{label}</span>
        <svg className="h-4 w-4 shrink-0 text-slate-500" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path d="M5.23 7.21a.75.75 0 0 1 1.06.02L10 11.06l3.71-3.83a.75.75 0 1 1 1.08 1.04l-4.25 4.39a.75.75 0 0 1-1.08 0L5.21 8.27a.75.75 0 0 1 .02-1.06z" />
        </svg>
      </button>
      {menu && (
        <SelectMenu
          id={listId}
          options={all}
          selected={value}
          initialQuery={menu.query}
          upward={menu.upward}
          onPick={(next) => {
            pick(next)
            close(true)
          }}
          onClose={close}
        />
      )}
    </div>
  )
}
