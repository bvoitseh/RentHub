import L from 'leaflet'
import { useEffect } from 'react'
import { markerIcon } from '@/components/map/leaflet'
import { useLeafletMap } from '@/components/map/useLeafletMap'

export interface MapViewProps {
  lat: number
  lon: number
  label: string
  className?: string
}

/** Read-only map with one pin (equipment card). */
export function MapView({ lat, lon, label, className = 'h-64' }: MapViewProps) {
  const { containerRef, map } = useLeafletMap({ lat, lon })

  useEffect(() => {
    if (!map) return
    const marker = L.marker([lat, lon], { icon: markerIcon, alt: label, keyboard: false }).addTo(map)
    map.setView([lat, lon])
    return () => {
      marker.remove()
    }
  }, [map, lat, lon, label])

  return <div ref={containerRef} role="region" aria-label={label} className={`relative z-0 w-full rounded-lg ${className}`} />
}
