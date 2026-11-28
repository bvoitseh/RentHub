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
