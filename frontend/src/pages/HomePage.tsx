import { uk } from '@/i18n/uk'

export function HomePage() {
  return (
    <section className="flex flex-col items-center gap-4 py-16 text-center">
      <h1 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">{uk.home.title}</h1>
      <p className="max-w-xl text-lg text-slate-600">{uk.home.subtitle}</p>
    </section>
  )
}
