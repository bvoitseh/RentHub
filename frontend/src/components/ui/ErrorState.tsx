import { Alert } from '@/components/ui/Alert'
import { Button } from '@/components/ui/Button'
import { problemMessage } from '@/api/problem'
import { uk } from '@/i18n/uk'

export interface ErrorStateProps {
  error: unknown
  onRetry?: () => void
}

/** Error state for a failed request: shows the backend's Ukrainian `detail` when available, with an optional retry. */
export function ErrorState({ error, onRetry }: ErrorStateProps) {
  return (
    <Alert tone="error" title={uk.common.errorTitle}>
      <p>{problemMessage(error)}</p>
      {onRetry && (
        <Button variant="secondary" className="mt-3" onClick={onRetry}>
          {uk.common.retry}
        </Button>
      )}
    </Alert>
  )
}
