import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'

export interface LatLon {
  lat: number
  lon: number
}

/** Tile server: OpenStreetMap by default (keep its attribution); configurable without code changes (NFR-13). */
const TILE_URL = import.meta.env.VITE_MAP_TILE_URL ?? 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const TILE_ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'

/** Start city (spec: Lviv) — the initial view when there is no point yet. */
export const DEFAULT_CENTER: LatLon = { lat: 49.8397, lon: 24.0297 }

/** Leaflet's default icon paths break under bundlers; the images are imported so Vite serves them. */
export const markerIcon = L.icon({
  iconUrl,
  iconRetinaUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  shadowSize: [41, 41],
})

export function createMap(element: HTMLElement, center: LatLon, zoom: number): L.Map {
  const map = L.map(element).setView([center.lat, center.lon], zoom)
  L.tileLayer(TILE_URL, { attribution: TILE_ATTRIBUTION, maxZoom: 19 }).addTo(map)
  return map
}

/** Coordinates rounded to ~10 cm, enough for a pin and stable in forms. */
export function roundPoint(lat: number, lon: number): LatLon {
  return { lat: Math.round(lat * 1e6) / 1e6, lon: Math.round(lon * 1e6) / 1e6 }
}
