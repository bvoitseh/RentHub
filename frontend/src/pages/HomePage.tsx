import { useNavigate } from 'react-router'
import { CatalogSearchBar } from '@/features/catalog/components/CatalogSearchBar'
import { CategoryLinks } from '@/features/catalog/components/CategoryLinks'
import { Recommendations } from '@/features/recommendations/components/Recommendations'
import { uk } from '@/i18n/uk'

/** Home: search box, categories and recommendations nearby (FR-16, FR-17). */
export function HomePage() {
  const navigate = useNavigate()

  return (
    <div className="flex flex-col gap-12">
      <section className="flex flex-col items-center gap-6 pt-12 text-center">
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">{uk.home.title}</h1>
        <p className="max-w-xl text-lg text-slate-600">{uk.home.subtitle}</p>
        <div className="w-full max-w-xl">
          <CatalogSearchBar onSearch={(q) => navigate(q ? `/catalog?q=${encodeURIComponent(q)}` : '/catalog')} />
        </div>
        <CategoryLinks />
      </section>
      <Recommendations />
    </div>
  )
}
