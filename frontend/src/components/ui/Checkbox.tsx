import type { InputHTMLAttributes, ReactNode } from 'react'

export interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: ReactNode
  description?: string
}

export function Checkbox({ label, description, id, className = '', ...rest }: CheckboxProps) {
  return (
    <label htmlFor={id} className={`flex cursor-pointer items-start gap-3 ${className}`}>
      <input
        id={id}
        type="checkbox"
        className="mt-0.5 h-4 w-4 rounded border-slate-300 accent-brand-600 focus:outline-2 focus:outline-brand-600"
        {...rest}
      />
      <span className="flex flex-col text-sm">
        <span className="font-medium text-slate-800">{label}</span>
        {description && <span className="text-slate-500">{description}</span>}
      </span>
    </label>
  )
}
