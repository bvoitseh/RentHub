const monthName = new Intl.DateTimeFormat('uk-UA', { month: 'long' })

/** Ukrainian month names in the nominative («січень»), index 1-12. */
export const months = Array.from({ length: 12 }, (_, i) => monthName.format(new Date(2026, i, 1)))

/** Numbers the Ukrainian way («11,5»), up to two decimals. */
export const formatNumber = (value: number, digits = 2) =>
  new Intl.NumberFormat('uk-UA', { minimumFractionDigits: 0, maximumFractionDigits: digits }).format(value)

export const monthLabel = (month: number): string => months[month - 1] ?? String(month)
