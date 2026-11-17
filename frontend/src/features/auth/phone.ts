/**
 * Ukrainian phone numbers in the UI use the mask +38 (0XX)-XXX-XX-XX; the API uses +380XXXXXXXXX.
 * Digits are the single source: "38" + a 10-digit national number starting with 0.
 */
export const PHONE_MASK = '+38 (0XX)-XXX-XX-XX'
export const PHONE_DISPLAY_PATTERN = /^\+38 \(0\d{2}\)-\d{3}-\d{2}-\d{2}$/

const MAX_DIGITS = 12

export function phoneDigits(value: string): string {
  return value.replace(/\D/g, '')
}

/** Puts typed or pasted digits into the "38" + "0XXXXXXXXX" shape (also accepts 0XX… or XX… without a prefix). */
function normalize(digits: string): string {
  if (digits.length < 3 && '38'.startsWith(digits)) return digits
  if (digits.startsWith('38')) return digits.slice(0, MAX_DIGITS)
  if (digits.startsWith('0')) return `38${digits}`.slice(0, MAX_DIGITS)
  return `380${digits}`.slice(0, MAX_DIGITS)
}

/** Formats digits progressively: "380671" → "+38 (067)-1", full number → "+38 (067)-123-45-67". */
export function formatPhone(rawDigits: string): string {
  const digits = normalize(rawDigits)
  if (digits.length === 0) return ''
  if (digits.length <= 2) return `+${digits}`
  const n = digits.slice(2)
  let out = `+38 (${n.slice(0, 3)}`
  if (n.length >= 3) out += ')'
  if (n.length > 3) out += `-${n.slice(3, 6)}`
  if (n.length > 6) out += `-${n.slice(6, 8)}`
  if (n.length > 8) out += `-${n.slice(8, 10)}`
  return out
}

/** "+38 (067)-123-45-67" → "+380671234567" (the API format). */
export function toApiPhone(display: string): string {
  return `+${phoneDigits(display)}`
}

/** "+380671234567" from the API → "+38 (067)-123-45-67"; empty when there is no phone. */
export function fromApiPhone(phone: string | null): string {
  return phone ? formatPhone(phoneDigits(phone)) : ''
}
