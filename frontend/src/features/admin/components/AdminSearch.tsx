import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { uk } from '@/i18n/uk'

/** Search box of the admin user and listing tabs. */
export function AdminSearch({ placeholder, onSearch }: { placeholder: string; onSearch: (q: string) => void }) {
  return (
    <form
      role="search"
      className="flex w-full max-w-lg gap-2"
      onSubmit={(event) => {
        event.preventDefault()
        onSearch(String(new FormData(event.currentTarget).get('q') ?? '').trim())
      }}
    >
      <Input name="q" type="search" maxLength={100} placeholder={placeholder} aria-label={placeholder} />
      <Button type="submit">{uk.admin.search}</Button>
    </form>
  )
}
