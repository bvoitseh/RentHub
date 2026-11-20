import type { OwnerType } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

export interface OwnerLabelProps {
  ownerType: OwnerType | null
  companyName: string | null
}

/** FR-1a: «Компанія · <name>» or «Приватна особа». */
export function OwnerLabel({ ownerType, companyName }: OwnerLabelProps) {
  if (!ownerType) return null
  const company = ownerType === 'COMPANY'
  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
        company ? 'bg-indigo-50 text-indigo-700' : 'bg-slate-100 text-slate-700'
      }`}
    >
      {uk.catalog.ownerTypes[ownerType]}
      {company && companyName ? ` · ${companyName}` : ''}
    </span>
  )
}
