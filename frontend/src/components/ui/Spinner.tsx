import { uk } from '@/i18n/uk'

const sizes = { sm: 'h-4 w-4 border-2', md: 'h-8 w-8 border-4', lg: 'h-12 w-12 border-4' } as const

export function Spinner({ size = 'md', className = '' }: { size?: keyof typeof sizes; className?: string }) {
  return (
    <span
      role="status"
      aria-label={uk.common.loading}
      className={`inline-block animate-spin rounded-full border-current border-t-transparent ${sizes[size]} ${className}`}
    />
  )
}

/** Centered loading state for a whole section or page. */
export function LoadingState({ label = uk.common.loading }: { label?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-slate-500">
      <Spinner />
      <span>{label}</span>
    </div>
  )
}
