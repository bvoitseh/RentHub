import { useCallback, useLayoutEffect, useState, type RefObject } from 'react'

const native = Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype, 'value') as PropertyDescriptor

/**
 * The value of the hidden native select behind the custom dropdown (D-51). The select stays the source of truth for
 * forms, react-hook-form and FormData, so its value is read back after every render (controlled values), after its
 * own change events, after a form reset, and whenever code assigns `select.value` (react-hook-form `setValue` /
 * `reset`, the catalogue filter reset) — the instance's `value` setter is wrapped for that.
 */
export function useSelectValue(ref: RefObject<HTMLSelectElement | null>, controlled: unknown, options: unknown) {
  const [value, setValue] = useState('')

  useLayoutEffect(() => {
    const el = ref.current
    if (!el) return
    const sync = () => setValue(native.get?.call(el) ?? '')
    Object.defineProperty(el, 'value', {
      configurable: true,
      get: () => native.get?.call(el),
      set: (next: string) => {
        native.set?.call(el, next)
        sync()
      },
    })
    const form = el.form
    const onReset = () => setTimeout(sync)
    el.addEventListener('change', sync)
    form?.addEventListener('reset', onReset)
    sync()
    return () => {
      Reflect.deleteProperty(el, 'value')
      el.removeEventListener('change', sync)
      form?.removeEventListener('reset', onReset)
    }
  }, [ref])

  // Controlled `value` props and new option lists are applied by React through the options, not through the setter.
  useLayoutEffect(() => {
    const current = ref.current ? (native.get?.call(ref.current) ?? '') : ''
    if (current !== value) setValue(current)
  }, [ref, value, controlled, options])

  /** Selects `next` like a user would: the value changes and bubbling input/change events fire. */
  const pick = useCallback(
    (next: string) => {
      const el = ref.current
      // Like a native select: choosing the current option changes nothing and fires no events.
      if (!el || native.get?.call(el) === next) return
      native.set?.call(el, next)
      el.dispatchEvent(new Event('input', { bubbles: true }))
      el.dispatchEvent(new Event('change', { bubbles: true }))
    },
    [ref],
  )

  return { value, pick }
}
