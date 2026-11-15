import { useBackendHealth } from '@/api/health'
import { uk } from '@/i18n/uk'

/** Small indicator in the footer: proves the /api proxy and the backend health endpoint work (NFR-2). */
export function BackendStatus() {
  const { data, isPending, isError } = useBackendHealth()

  const state = isPending ? 'pending' : isError || data?.status !== 'UP' ? 'down' : 'up'
  const label = { pending: uk.status.checking, up: uk.status.serverUp, down: uk.status.serverDown }[state]
  const dot = { pending: 'bg-slate-300', up: 'bg-emerald-500', down: 'bg-red-500' }[state]

  return (
    <span className="inline-flex items-center gap-2" role="status">
      <span aria-hidden className={`h-2 w-2 rounded-full ${dot}`} />
      {label}
    </span>
  )
}
