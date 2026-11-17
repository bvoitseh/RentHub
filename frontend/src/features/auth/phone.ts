/**
 * Ukrainian phone numbers in the UI use the mask +380 (XX)-XXX-XX-XX; the API uses +380XXXXXXXXX.
 * Digits are the single source: "380" + a 9-digit subscriber number.
 */
export const PHONE_MASK = '+380 (XX)-XXX-XX-XX'
export const PHONE_DISPLAY_PATTERN = /^\+380 \(\d{2}\)-\d{3}-\d{2}-\d{2}$/

const PREFIX = '380'
const MAX_DIGITS = 12

export function phoneDigits(value: string): string {
  return value.replace(/\D/g, '')
}

/** Puts typed or pasted digits into the "380" + 9 digits shape (also accepts "067…" or "67…" without the prefix). */
function normalize(digits: string): string {
  if (digits.length < PREFIX.length && PREFIX.startsWith(digits)) return digits
  if (digits.startsWith(PREFIX)) return digits.slice(0, MAX_DIGITS)
  if (digits.startsWith('0')) return `38${digits}`.slice(0, MAX_DIGITS)
  return `${PREFIX}${digits}`.slice(0, MAX_DIGITS)
}

/** Formats digits progressively: "38067" → "+380 (67)", full number → "+380 (67)-123-45-67". */
export function formatPhone(rawDigits: string): string {
  const digits = normalize(rawDigits)
  if (digits.length === 0) return ''
  if (digits.length <= PREFIX.length) return `+${digits}`
  const n = digits.slice(PREFIX.length)
  let out = `+${PREFIX} (${n.slice(0, 2)}`
  if (n.length >= 2) out += ')'
  if (n.length > 2) out += `-${n.slice(2, 5)}`
  if (n.length > 5) out += `-${n.slice(5, 7)}`
  if (n.length > 7) out += `-${n.slice(7, 9)}`
  return out
}

/** "+380 (67)-123-45-67" → "+380671234567" (the API format). */
export function toApiPhone(display: string): string {
  return `+${phoneDigits(display)}`
}

/** "+380671234567" from the API → "+380 (67)-123-45-67"; empty when there is no phone. */
export function fromApiPhone(phone: string | null): string {
  return phone ? formatPhone(phoneDigits(phone)) : ''
}
