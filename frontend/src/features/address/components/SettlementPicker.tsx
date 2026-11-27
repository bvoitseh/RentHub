import { useState } from 'react'
import { FormField } from '@/components/ui/FormField'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { SettlementSuggestions } from '@/features/address/components/SettlementSuggestions'
import { useRegions, useSettlementSuggestions } from '@/features/address/hooks/useAddress'
import { toSettlementValue, type SettlementValue } from '@/features/address/types'
import { uk } from '@/i18n/uk'

const t = uk.address

export interface SettlementPickerProps {
  idPrefix: string
  value: SettlementValue | null
  onChange: (value: SettlementValue | null) => void
  error?: string
}

/** «Область» + «Населений пункт» with suggestions from our database (D-48); only a picked suggestion counts. */
export function SettlementPicker({ idPrefix, value, onChange, error }: SettlementPickerProps) {
  const regions = useRegions()
  const [regionId, setRegionId] = useState<number | null>(value?.regionId ?? null)
  const [text, setText] = useState(value?.label ?? '')
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(0)
  const suggestions = useSettlementSuggestions(regionId, text)
  const items = suggestions.enabled ? (suggestions.data ?? []) : []
  const listId = `${idPrefix}-settlements`

  const pick = (index: number) => {
    const chosen = items[index]
    if (!chosen) return
    onChange(toSettlementValue(chosen))
    setText(chosen.label)
    setOpen(false)
  }

  return (
    <div className="flex flex-col gap-3">
      <FormField label={t.region} htmlFor={`${idPrefix}-region`}>
        <Select
          id={`${idPrefix}-region`}
          placeholder={t.regionPlaceholder}
          options={(regions.data ?? []).map((r) => ({ value: String(r.id), label: r.name }))}
          value={regionId === null ? '' : String(regionId)}
          onChange={(event) => {
            setRegionId(event.target.value ? Number(event.target.value) : null)
            setText('')
            onChange(null)
          }}
        />
      </FormField>
      <FormField label={t.settlement} htmlFor={`${idPrefix}-settlement`} error={error} hint={t.settlementHint}>
        <div className="relative">
          <Input
            id={`${idPrefix}-settlement`}
            role="combobox"
            aria-expanded={open && suggestions.enabled}
            aria-controls={listId}
            aria-autocomplete="list"
            autoComplete="off"
            disabled={regionId === null}
            placeholder={regionId === null ? t.chooseRegionFirst : t.settlementPlaceholder}
            invalid={!!error}
            value={text}
            onChange={(event) => {
              setText(event.target.value)
              setOpen(true)
              setActive(0)
              if (value) onChange(null)
            }}
            onFocus={() => setOpen(true)}
            onBlur={() => setOpen(false)}
            onKeyDown={(event) => {
              if (event.key === 'ArrowDown') setActive((i) => Math.min(i + 1, items.length - 1))
              else if (event.key === 'ArrowUp') setActive((i) => Math.max(i - 1, 0))
              else if (event.key === 'Enter' && open && items.length > 0) {
                event.preventDefault()
                pick(active)
              } else if (event.key === 'Escape') setOpen(false)
              else return
              event.preventDefault()
            }}
          />
          {open && suggestions.enabled && !value && (
            <SettlementSuggestions
              id={listId}
              items={items}
              loading={suggestions.isFetching}
              active={active}
              onPick={(s) => pick(items.indexOf(s))}
            />
          )}
        </div>
      </FormField>
    </div>
  )
}
