import { useState } from 'react'
import type { DateRange } from '@/features/booking/types'

/**
 * Two-click period selection: the first click picks the first day, the second the last day (an earlier day restarts
 * the selection); a click after a complete period starts a new one.
 */
export function useDateRange() {
  const [range, setRange] = useState<DateRange | null>(null)

  const pick = (day: string) =>
    setRange((current) => {
      if (!current || current.to !== null || day < current.from) return { from: day, to: null }
      return { from: current.from, to: day }
    })

  return { range, pick, clear: () => setRange(null) }
}
