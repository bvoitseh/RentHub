const money = new Intl.NumberFormat('uk-UA', { maximumFractionDigits: 2 })
const dayMonthYear = new Intl.DateTimeFormat('uk-UA', { day: 'numeric', month: 'long', year: 'numeric' })

/** "1 250,5 ₴" */
export function formatPrice(value: number): string {
  return `${money.format(value)} ₴`
}

/**
 * The month and year in the genitive case, as Ukrainian needs after «з»: "серпня 2026 р." (a month-only format would
 * give the nominative "серпень"). Built from a full date with the day and the space after it removed.
 */
export function formatMonth(iso: string): string {
  const parts = dayMonthYear.formatToParts(new Date(iso))
  const monthIndex = parts.findIndex((p) => p.type === 'month')
  return parts.slice(monthIndex).map((p) => p.value).join('')
}
