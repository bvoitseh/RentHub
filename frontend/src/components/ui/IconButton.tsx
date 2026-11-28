import type { ButtonHTMLAttributes, ReactNode } from 'react'

type Tone = 'neutral' | 'danger' | 'primary'

export interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  /** Accessible name; also shown as the tooltip. */
  label: string
  tone?: Tone
  children: ReactNode
}

const tones: Record<Tone, string> = {
  neutral: 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
  danger: 'text-red-600 hover:bg-red-50',
  primary: 'bg-brand-600 text-on-accent hover:bg-brand-700',
}

/** A round button showing only an icon; the label is the accessible name and the tooltip. */
export function IconButton({ label, tone = 'neutral', className = '', children, ...rest }: IconButtonProps) {
  return (
    <button
      type="button"
      {...rest}
      aria-label={label}
      title={label}
      className={`inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-full transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 disabled:cursor-not-allowed disabled:opacity-50 ${tones[tone]} ${className}`}
    >
      {children}
    </button>
  )
}
