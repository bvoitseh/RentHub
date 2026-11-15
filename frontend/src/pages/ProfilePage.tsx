import { DeactivateAccountCard } from '@/features/auth/components/DeactivateAccountCard'
import { GeoConsentCard } from '@/features/auth/components/GeoConsentCard'
import { ProfileForm } from '@/features/auth/components/ProfileForm'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { uk } from '@/i18n/uk'

/** Rendered inside RequireAuth, so the user is always present here. */
export function ProfilePage() {
  const { data: user } = useCurrentUser()
  if (!user) return null

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-6">
      <h1 className="text-2xl font-bold text-slate-900">{uk.profile.title}</h1>
      <section className="rounded-lg border border-slate-200 bg-white p-6">
        <h2 className="mb-4 text-lg font-semibold text-slate-900">{uk.profile.edit.title}</h2>
        <ProfileForm user={user} />
      </section>
      <GeoConsentCard user={user} />
      <DeactivateAccountCard />
    </div>
  )
}
