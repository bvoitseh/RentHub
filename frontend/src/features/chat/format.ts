const timeFormat = new Intl.DateTimeFormat('uk-UA', { hour: '2-digit', minute: '2-digit' })
const dayFormat = new Intl.DateTimeFormat('uk-UA', { day: 'numeric', month: 'short' })

/** «14:05» today, «3 груд., 14:05» on other days. */
export function formatMessageTime(instant: string) {
  const date = new Date(instant)
  const sameDay = date.toDateString() === new Date().toDateString()
  return sameDay ? timeFormat.format(date) : `${dayFormat.format(date)}, ${timeFormat.format(date)}`
}

/** «m:ss» for the voice recorder. */
export function formatDuration(totalSeconds: number) {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

/** «850 КБ», «4,2 МБ». */
export function formatSize(bytes: number) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} КБ`
  return `${(bytes / (1024 * 1024)).toLocaleString('uk-UA', { maximumFractionDigits: 1 })} МБ`
}

export interface LastSeenLabels {
  justNow: string
  today: (time: string) => string
  yesterday: (time: string) => string
  onDay: (day: string, time: string) => string
}

/** «щойно» / «сьогодні о 14:05» / «вчора о 09:10» / «3 груд. о 18:20» for the last-seen line (D-58). */
export function formatLastSeen(instant: string, labels: LastSeenLabels) {
  const date = new Date(instant)
  if (Date.now() - date.getTime() < 60_000) return labels.justNow
  const time = timeFormat.format(date)
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === new Date().toDateString()) return labels.today(time)
  if (date.toDateString() === yesterday.toDateString()) return labels.yesterday(time)
  return labels.onDay(dayFormat.format(date), time)
}
