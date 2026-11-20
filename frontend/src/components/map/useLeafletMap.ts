import type L from 'leaflet'
import { useCallback, useRef, useState } from 'react'
import { createMap, type LatLon } from '@/components/map/leaflet'

/**
 * Creates a Leaflet map in the element the returned callback ref is attached to, and removes it on unmount
 * (React 19 ref cleanup). `initialCenter` is used once; move the view through the returned map afterwards.
 */
export function useLeafletMap(initialCenter: LatLon, zoom = 15) {
  const center = useRef(initialCenter)
  const [map, setMap] = useState<L.Map | null>(null)

  const containerRef = useCallback(
    (element: HTMLDivElement | null) => {
      if (!element) return
      const created = createMap(element, center.current, zoom)
      setMap(created)
      return () => {
        created.remove()
        setMap(null)
      }
    },
    [zoom],
  )

  return { containerRef, map }
}
