import type { InputHTMLAttributes, Ref } from 'react'

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  invalid?: boolean
  /** React 19 passes ref as a regular prop; react-hook-form uses it to focus the field on errors. */
  ref?: Ref<HTMLInputElement>
}

const base =
  'block w-full rounded-md border bg-surface px-3 py-2 text-sm text-slate-900 shadow-xs placeholder:text-slate-400 ' +
  'focus:outline-2 focus:outline-offset-0 disabled:bg-slate-100 disabled:text-slate-500'

export function Input({ invalid = false, className = '', ...rest }: InputProps) {
  const border = invalid ? 'border-red-400 focus:outline-red-500' : 'border-slate-300 focus:outline-brand-600'
  return <input aria-invalid={invalid || undefined} className={`${base} ${border} ${className}`} {...rest} />
}
