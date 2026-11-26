import type { LatLon } from '@/components/map/leaflet'
import { Button } from '@/components/ui/Button'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useBrowserLocation } from '@/features/catalog/hooks/useBrowserLocation'
import { uk } from '@/i18n/uk'

const t = uk.recommendations

export interface LocationControlProps {
  point: LatLon | null
  onChange: (point: LatLon | null) => void
}

/**
 * FR-16: a signed-in user with geolocation consent (FR-3) may use the browser location for this request only;
 * without consent a hint points to the profile; guests see nothing here (start city).
 */
export function LocationControl({ point, onChange }: LocationControlProps) {
  const { data: user } = useCurrentUser()
  const { status, locate } = useBrowserLocation()
  if (!user || user.role !== 'USER') return null
  if (!user.geoConsent) return <p className="text-xs text-slate-500">{t.consentHint}</p>

  return (
    <div className="flex flex-wrap items-center gap-2 text-sm">
      {point ? (
        <Button variant="secondary" onClick={() => onChange(null)}>
          {t.useCity}
        </Button>
      ) : (
        <Button variant="secondary" loading={status === 'locating'} onClick={() => void locate().then((p) => p && onChange(p))}>
          {t.useLocation}
        </Button>
      )}
      {status === 'denied' && <span className="text-red-700">{t.denied}</span>}
      <span className="text-xs text-slate-500">{t.privacy}</span>
    </div>
  )
}
