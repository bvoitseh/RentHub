const money = new Intl.NumberFormat('uk-UA', { maximumFractionDigits: 2 })
const date = new Intl.DateTimeFormat('uk-UA', { year: 'numeric', month: 'long' })

/** "1 250,5 ₴" */
export function formatPrice(value: number): string {
  return `${money.format(value)} ₴`
}

/** "листопад 2026 р." */
export function formatMonth(iso: string): string {
  return date.format(new Date(iso))
}
