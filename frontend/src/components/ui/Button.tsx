import type { ButtonHTMLAttributes } from 'react'
import { Spinner } from '@/components/ui/Spinner'

type Variant = 'primary' | 'secondary' | 'danger' | 'danger-soft' | 'ghost'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  loading?: boolean
}

const base =
  'inline-flex items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-medium transition-colors ' +
  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 disabled:cursor-not-allowed disabled:opacity-60'

const variants: Record<Variant, string> = {
  primary: 'bg-brand-600 text-on-accent hover:bg-brand-700',
  secondary: 'border border-slate-300 bg-surface text-slate-800 hover:bg-slate-50',
  danger: 'bg-red-600 text-white hover:bg-red-700 dark:hover:bg-red-500',
  // A quieter red (e.g. «Вийти»): tinted background, red text; these shades are remapped in the dark theme.
  'danger-soft': 'border border-red-200 bg-red-50 text-red-700 hover:border-red-400',
  ghost: 'text-slate-700 hover:bg-slate-100',
}

export function Button({ variant = 'primary', loading = false, disabled, className = '', children, ...rest }: ButtonProps) {
  return (
    <button
      type="button"
      {...rest}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={`${base} ${variants[variant]} ${className}`}
    >
      {loading && <Spinner size="sm" />}
      {children}
    </button>
  )
}
