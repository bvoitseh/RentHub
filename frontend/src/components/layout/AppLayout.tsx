import { Outlet } from 'react-router'
import { Footer } from '@/components/layout/Footer'
import { Header } from '@/components/layout/Header'
import { Sidebar } from '@/components/layout/Sidebar'
import { VerifyEmailBanner } from '@/features/auth/components/VerifyEmailBanner'
import { ChatSocketBridge } from '@/features/chat/components/ChatSocketBridge'

/** Base page frame: sticky header, side menu on wide screens (D-53), verification banner, content, footer. */
export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <ChatSocketBridge />
      <Header />
      <div className="flex flex-1">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <VerifyEmailBanner />
          <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-6">
            <Outlet />
          </main>
        </div>
      </div>
      <Footer />
    </div>
  )
}
