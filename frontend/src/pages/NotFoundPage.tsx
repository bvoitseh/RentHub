import { Link } from 'react-router'
import { EmptyState } from '@/components/ui/EmptyState'
import { uk } from '@/i18n/uk'

export function NotFoundPage() {
  return (
    <EmptyState
      title={uk.notFound.title}
      description={uk.notFound.description}
      action={
        <Link to="/" className="text-sm font-medium text-brand-700 hover:underline">
          {uk.notFound.backHome}
        </Link>
      }
    />
  )
}
