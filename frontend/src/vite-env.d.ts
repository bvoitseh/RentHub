/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Optional map tile URL template; defaults to OpenStreetMap tiles. */
  readonly VITE_MAP_TILE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
