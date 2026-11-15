import type { ReactNode } from 'react'

export interface FormFieldProps {
  label: string
  htmlFor: string
  error?: string
  hint?: string
  children: ReactNode
}

/** Label + control + hint/error line. Pass the control's id as `htmlFor`; the error is announced via aria-live. */
export function FormField({ label, htmlFor, error, hint, children }: FormFieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={htmlFor} className="text-sm font-medium text-slate-700">
        {label}
      </label>
      {children}
      <p className={`min-h-4 text-xs ${error ? 'text-red-600' : 'text-slate-500'}`} aria-live="polite">
        {error ?? hint}
      </p>
    </div>
  )
}
