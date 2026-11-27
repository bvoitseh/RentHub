import type { ReactNode } from 'react'

export interface AuthCardProps {
  title: string
  children: ReactNode
  footer?: ReactNode
}

/** Centered card used by the login / register / reset pages. */
export function AuthCard({ title, children, footer }: AuthCardProps) {
  return (
    <div className="mx-auto w-full max-w-md">
      <div className="rounded-lg border border-slate-200 bg-surface p-6 shadow-xs sm:p-8">
        <h1 className="mb-6 text-2xl font-bold text-slate-900">{title}</h1>
        {children}
      </div>
      {footer && <div className="mt-4 text-center text-sm text-slate-600">{footer}</div>}
    </div>
  )
}
