import { problemMessage } from '@/api/problem'
import { Alert } from '@/components/ui/Alert'
import { Checkbox } from '@/components/ui/Checkbox'
import { useSetGeoConsent } from '@/features/auth/hooks/useAccountMutations'
import type { User } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

const t = uk.profile.geo

/** FR-3: give or withdraw geolocation consent; withdrawal takes effect immediately. */
export function GeoConsentCard({ user }: { user: User }) {
  const consent = useSetGeoConsent()

  return (
    <section className="rounded-lg border border-slate-200 bg-surface p-6">
      <h2 className="text-lg font-semibold text-slate-900">{t.title}</h2>
      <p className="mt-1 text-sm text-slate-600">{t.text}</p>
      <Checkbox
        id="geo-consent"
        className="mt-4"
        label={t.label}
        checked={user.geoConsent}
        disabled={consent.isPending}
        onChange={(event) => consent.mutate(event.target.checked)}
      />
      {consent.isError && (
        <Alert tone="error" className="mt-3">
          {problemMessage(consent.error)}
        </Alert>
      )}
      {consent.isSuccess && (
        <Alert tone="success" className="mt-3">
          {consent.data.geoConsent ? t.granted : t.withdrawn}
        </Alert>
      )}
    </section>
  )
}
