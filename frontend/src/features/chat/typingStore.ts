import { useEffect, useState, useSyncExternalStore } from 'react'

/** How long «друкує…» stays after the last typing signal (D-58). */
const TYPING_MS = 4000

const typingAt = new Map<number, number>()
const listeners = new Set<() => void>()
const emit = () => listeners.forEach((listener) => listener())

/** The other side typed in this conversation just now. */
export function markTyping(conversationId: number) {
  typingAt.set(conversationId, Date.now())
  emit()
}

/** A message arrived: the typing hint goes away at once. */
export function clearTyping(conversationId: number) {
  if (typingAt.delete(conversationId)) emit()
}

function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

/** True while the other side typed within the last {@link TYPING_MS} ms. */
export function useTyping(conversationId: number) {
  const at = useSyncExternalStore(subscribe, () => typingAt.get(conversationId) ?? 0)
  // The signal (identified by its time) that has run out; a newer signal shows the hint again.
  const [expired, setExpired] = useState(0)
  useEffect(() => {
    if (!at) return
    const timer = window.setTimeout(() => setExpired(at), TYPING_MS)
    return () => window.clearTimeout(timer)
  }, [at])
  return at > 0 && expired !== at
}
