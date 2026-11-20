import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { uk } from '@/i18n/uk'

export interface CatalogSearchBarProps {
  initial?: string
  onSearch: (q: string) => void
}

/** Keyword search (FR-7); used on the home page and above the catalogue. */
export function CatalogSearchBar({ initial = '', onSearch }: CatalogSearchBarProps) {
  return (
    <form
      role="search"
      className="flex w-full gap-2"
      onSubmit={(event) => {
        event.preventDefault()
        onSearch(String(new FormData(event.currentTarget).get('q') ?? '').trim())
      }}
    >
      <Input
        key={initial}
        name="q"
        type="search"
        defaultValue={initial}
        maxLength={100}
        placeholder={uk.catalog.searchPlaceholder}
        aria-label={uk.catalog.searchPlaceholder}
      />
      <Button type="submit">{uk.catalog.search}</Button>
    </form>
  )
}
