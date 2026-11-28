/** Initials of a name in a coloured circle (stable colour per user), with a green dot while online (D-58). */
const COLORS = ['bg-brand-600', 'bg-sky-600', 'bg-indigo-600', 'bg-emerald-600', 'bg-amber-600', 'bg-red-600']

export function ChatAvatar({ id, name, online = false }: { id: number; name: string; online?: boolean }) {
  const initials = name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase())
    .join('')
  return (
    <span className="relative shrink-0" aria-hidden="true">
      <span
        className={`flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold text-white ${COLORS[id % COLORS.length]}`}
      >
        {initials || '?'}
      </span>
      {online && (
        <span className="absolute right-0 bottom-0 h-3 w-3 rounded-full border-2 border-surface bg-emerald-500" />
      )}
    </span>
  )
}
