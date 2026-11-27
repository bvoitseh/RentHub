import { Link } from 'react-router'
import { useCategories } from '@/features/catalog/hooks/useCatalogQueries'

/** Category shortcuts into the catalogue (home page). */
export function CategoryLinks() {
  const { data: categories = [] } = useCategories()
  if (categories.length === 0) return null

  return (
    <ul className="flex flex-wrap justify-center gap-2">
      {categories.map((category) => (
        <li key={category.id}>
          <Link
            to={`/catalog?categoryId=${category.id}`}
            className="inline-block rounded-full border border-slate-300 bg-surface px-4 py-1.5 text-sm text-slate-700 hover:border-brand-500 hover:text-brand-700"
          >
            {category.name}
          </Link>
        </li>
      ))}
    </ul>
  )
}
