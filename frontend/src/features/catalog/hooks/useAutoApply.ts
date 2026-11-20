import { useCallback, useEffect, useRef } from 'react'

const IMMEDIATE_TYPES = new Set(['checkbox', 'radio', 'select-one', 'date'])

/**
 * Applies a form automatically: choices (selects, checkboxes, dates) at once, typed values after a pause, so the
 * catalogue is not re-queried on every keystroke. Returns the form's onChange handler.
 */
export function useAutoApply(apply: (form: HTMLFormElement) => void, delayMs = 500) {
  const timer = useRef<number | undefined>(undefined)

  useEffect(() => () => window.clearTimeout(timer.current), [])

  return useCallback(
    (event: React.FormEvent<HTMLFormElement>) => {
      const form = event.currentTarget
      const target = event.target as HTMLInputElement | HTMLSelectElement
      window.clearTimeout(timer.current)
      if (IMMEDIATE_TYPES.has(target.type)) apply(form)
      else timer.current = window.setTimeout(() => apply(form), delayMs)
    },
    [apply, delayMs],
  )
}
