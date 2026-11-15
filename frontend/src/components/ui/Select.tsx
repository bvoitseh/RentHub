import type { SelectHTMLAttributes } from 'react'

export interface SelectOption {
  value: string
  label: string
}

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  options: SelectOption[]
  invalid?: boolean
  placeholder?: string
}

const base =
  'block w-full rounded-md border bg-white px-3 py-2 text-sm text-slate-900 shadow-xs ' +
  'focus:outline-2 focus:outline-offset-0 disabled:bg-slate-100 disabled:text-slate-500'

export function Select({ options, invalid = false, placeholder, className = '', ...rest }: SelectProps) {
  const border = invalid ? 'border-red-400 focus:outline-red-500' : 'border-slate-300 focus:outline-brand-600'
  return (
    <select aria-invalid={invalid || undefined} className={`${base} ${border} ${className}`} {...rest}>
      {placeholder !== undefined && <option value="">{placeholder}</option>}
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  )
}
