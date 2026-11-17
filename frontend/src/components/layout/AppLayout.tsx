import { Outlet } from 'react-router'
import { Footer } from '@/components/layout/Footer'
import { Header } from '@/components/layout/Header'
import { VerifyEmailBanner } from '@/features/auth/components/VerifyEmailBanner'

/** Base page frame: sticky header, verification banner, content area with a responsive container, footer. */
export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <VerifyEmailBanner />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-6">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
