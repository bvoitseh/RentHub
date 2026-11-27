/** D-48: reference data served by our backend (OpenStreetMap + Wikidata). */
export interface Region {
  id: number
  name: string
}

export type SettlementStatus = 'CITY' | 'TOWN' | 'VILLAGE'

export interface Settlement {
  id: number
  name: string
  /** e.g. «с. Миколаївка, Лихівська громада, Кам'янський р-н» */
  label: string
  status: SettlementStatus | null
  regionId: number
  regionName: string
}

/** What a form keeps for the chosen settlement. */
export interface SettlementValue {
  id: number
  label: string
  regionId: number
}

export const toSettlementValue = (s: Settlement | null): SettlementValue | null =>
  s ? { id: s.id, label: s.label, regionId: s.regionId } : null
