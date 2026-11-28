/** Initials of a name in a coloured circle; the colour is stable per user. */
const COLORS = ['bg-brand-600', 'bg-sky-600', 'bg-indigo-600', 'bg-emerald-600', 'bg-amber-600', 'bg-red-600']

export function ChatAvatar({ id, name }: { id: number; name: string }) {
  const initials = name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase())
    .join('')
  return (
    <span
      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold text-white ${COLORS[id % COLORS.length]}`}
      aria-hidden="true"
    >
      {initials || '?'}
    </span>
  )
}
