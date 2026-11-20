import { useCallback, useState } from 'react'
import { roundPoint, type LatLon } from '@/components/map/leaflet'

type Status = 'idle' | 'locating' | 'denied'

/**
 * One-off browser geolocation for "near me" (NFR-5: asked only on the user's action, used only for this search,
 * never stored). Coordinates are rounded to ~100 m.
 */
export function useBrowserLocation() {
  const [status, setStatus] = useState<Status>('idle')

  const locate = useCallback(
    () =>
      new Promise<LatLon | null>((resolve) => {
        if (!('geolocation' in navigator)) {
          setStatus('denied')
          resolve(null)
          return
        }
        setStatus('locating')
        navigator.geolocation.getCurrentPosition(
          (position) => {
            setStatus('idle')
            const { lat, lon } = roundPoint(position.coords.latitude, position.coords.longitude)
            resolve({ lat: Math.round(lat * 1000) / 1000, lon: Math.round(lon * 1000) / 1000 })
          },
          () => {
            setStatus('denied')
            resolve(null)
          },
          { timeout: 10_000, maximumAge: 5 * 60_000 },
        )
      }),
    [],
  )

  return { locate, status }
}
