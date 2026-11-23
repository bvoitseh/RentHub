/** Calendar dates as ISO "YYYY-MM-DD" strings in the user's local time (the business zone is Europe/Kyiv). */

const pad = (n: number) => String(n).padStart(2, '0')

export function toIso(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function fromIso(iso: string): Date {
  return new Date(Number(iso.slice(0, 4)), Number(iso.slice(5, 7)) - 1, Number(iso.slice(8, 10)))
}

/** "Today" is the business date in Europe/Kyiv, as on the server, wherever the browser is. */
const kyivDate = new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Kyiv', year: 'numeric', month: '2-digit', day: '2-digit' })

export function todayIso(): string {
  return kyivDate.format(new Date())
}

export function addDays(iso: string, days: number): string {
  const d = fromIso(iso)
  d.setDate(d.getDate() + days)
  return toIso(d)
}

/** Inclusive day count of [from, to] (FR-10: dateTo − dateFrom + 1). */
export function daysBetween(from: string, to: string): number {
  return Math.round((fromIso(to).getTime() - fromIso(from).getTime()) / 86_400_000) + 1
}

/** First and last day of the month that `monthStart` (YYYY-MM-01) belongs to. */
export function monthBounds(monthStart: string): { from: string; to: string } {
  const d = fromIso(monthStart)
  return { from: toIso(new Date(d.getFullYear(), d.getMonth(), 1)), to: toIso(new Date(d.getFullYear(), d.getMonth() + 1, 0)) }
}

export function shiftMonth(monthStart: string, delta: number): string {
  const d = fromIso(monthStart)
  return toIso(new Date(d.getFullYear(), d.getMonth() + delta, 1))
}

/** The month grid: leading nulls so that the first day lands on its weekday (Monday first). */
export function monthGrid(monthStart: string): (string | null)[] {
  const { from, to } = monthBounds(monthStart)
  const lead = (fromIso(from).getDay() + 6) % 7
  const cells: (string | null)[] = Array.from({ length: lead }, () => null)
  for (let day = from; day <= to; day = addDays(day, 1)) cells.push(day)
  return cells
}

const dateFormat = new Intl.DateTimeFormat('uk-UA', { day: 'numeric', month: 'short', year: 'numeric' })
const monthFormat = new Intl.DateTimeFormat('uk-UA', { month: 'long', year: 'numeric' })
const dateTimeFormat = new Intl.DateTimeFormat('uk-UA', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })

/** "21 лист. 2026 р." */
export const formatDate = (iso: string) => dateFormat.format(fromIso(iso))
/** "Листопад 2026 р." — only the first letter capitalised (CSS `capitalize` would also give "Р."). */
export const formatMonthTitle = (monthStart: string) => {
  const title = monthFormat.format(fromIso(monthStart))
  return title.charAt(0).toUpperCase() + title.slice(1)
}
/** For instants (e.g. the response deadline). */
export const formatDateTime = (instant: string) => dateTimeFormat.format(new Date(instant))
/** "21 лист. 2026 р. — 23 лист. 2026 р." or one date for a single day. */
export const formatPeriod = (from: string, to: string) => (from === to ? formatDate(from) : `${formatDate(from)} — ${formatDate(to)}`)
