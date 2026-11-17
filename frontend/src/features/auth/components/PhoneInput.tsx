import { Input, type InputProps } from '@/components/ui/Input'
import { formatPhone, PHONE_MASK, phoneDigits } from '@/features/auth/phone'

export interface PhoneInputProps extends Omit<InputProps, 'value' | 'onChange' | 'type'> {
  value: string
  onChange: (value: string) => void
}

/**
 * Phone field with the +38 (0XX)-XXX-XX-XX mask applied while typing, pasting and deleting. Accepts "067…", "67…"
 * or "+380…"; the empty field shows the mask as its placeholder.
 */
export function PhoneInput({ value, onChange, ...rest }: PhoneInputProps) {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const next = event.target.value
    let digits = phoneDigits(next)
    // Deleting only a mask character ("-", ")" or a space) would change nothing; remove the digit before it instead.
    if (next.length < value.length && digits === phoneDigits(value)) digits = digits.slice(0, -1)
    onChange(formatPhone(digits))
  }

  return (
    <Input
      {...rest}
      type="tel"
      inputMode="tel"
      autoComplete="tel"
      placeholder={PHONE_MASK}
      maxLength={PHONE_MASK.length}
      value={value}
      onChange={handleChange}
    />
  )
}
