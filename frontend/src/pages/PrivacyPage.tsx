import { uk } from '@/i18n/uk'

const t = uk.privacy

/** NFR-5: what data RentHub collects, who sees the phone, geolocation, third parties and retention. */
export function PrivacyPage() {
  return (
    <article className="mx-auto flex w-full max-w-3xl flex-col gap-6 text-slate-700">
      <header className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-slate-900">{t.title}</h1>
        <p className="text-sm text-slate-500">{t.updated}</p>
        <p>{t.intro}</p>
      </header>
      {t.sections.map((section) => (
        <section key={section.title} className="flex flex-col gap-2">
          <h2 className="text-lg font-semibold text-slate-900">{section.title}</h2>
          <ul className="flex list-disc flex-col gap-1 pl-5">
            {section.items.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </section>
      ))}
    </article>
  )
}
