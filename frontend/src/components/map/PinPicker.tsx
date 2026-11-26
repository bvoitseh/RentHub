import L from 'leaflet'
import { useEffect, useRef, useState } from 'react'
import { DEFAULT_CENTER, markerIcon, roundPoint, type LatLon } from '@/components/map/leaflet'
import { useLeafletMap } from '@/components/map/useLeafletMap'

export interface PinPickerProps {
  value: LatLon | null
  onChange: (point: LatLon) => void
  label: string
  className?: string
}

/** FR-4 pin editor: click the map to place the pin, drag it to correct; external value changes move the pin. */
export function PinPicker({ value, onChange, label, className = 'h-72' }: PinPickerProps) {
  // The zoom only matters when the map is created; a changing value must not re-create the map under the pin.
  const [initialZoom] = useState(value ? 16 : 13)
  const { containerRef, map } = useLeafletMap(value ?? DEFAULT_CENTER, initialZoom)
  const marker = useRef<L.Marker | null>(null)
  const onChangeRef = useRef(onChange)

  useEffect(() => {
    onChangeRef.current = onChange
  }, [onChange])

  useEffect(() => {
    if (!map) return
    const onClick = (event: L.LeafletMouseEvent) => onChangeRef.current(roundPoint(event.latlng.lat, event.latlng.lng))
    map.on('click', onClick)
    return () => {
      map.off('click', onClick)
    }
  }, [map])

  const lat = value?.lat
  const lon = value?.lon
  useEffect(() => {
    if (!map) return
    if (lat === undefined || lon === undefined) {
      marker.current?.remove()
      marker.current = null
      return
    }
    if (!marker.current) {
      const created = L.marker([lat, lon], { icon: markerIcon, draggable: true, alt: label }).addTo(map)
      created.on('dragend', () => {
        const position = created.getLatLng()
        onChangeRef.current(roundPoint(position.lat, position.lng))
      })
      marker.current = created
    } else {
      marker.current.setLatLng([lat, lon])
    }
    map.panTo([lat, lon])
  }, [map, lat, lon, label])

  useEffect(() => () => {
    marker.current = null
  }, [map])

  return (
    <div
      ref={containerRef}
      role="region"
      aria-label={label}
      className={`relative z-0 w-full cursor-crosshair rounded-lg border border-slate-300 ${className}`}
    />
  )
}
