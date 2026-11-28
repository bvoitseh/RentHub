import type { ComponentType } from 'react'
import { createBrowserRouter } from 'react-router'
import { AppLayout } from '@/components/layout/AppLayout'
import { RequireAuth } from '@/features/auth/components/RequireAuth'
import type { Role } from '@/features/auth/types'
import { HomePage } from '@/pages/HomePage'
import { NotFoundPage } from '@/pages/NotFoundPage'

/** Lazy route whose page renders only for logged-in users (guests are sent to /login and back). */
function protectedRoute(path: string, load: () => Promise<ComponentType>, roles?: Role[]) {
  return {
    path,
    lazy: {
      Component: async () => {
        const Page = await load()
        return function ProtectedPage() {
          return (
            <RequireAuth roles={roles}>
              <Page />
            </RequireAuth>
          )
        }
      },
    },
  }
}

/** Route table. Feature pages are code-split per route; pages only compose components. */
export const router = createBrowserRouter([
  {
    path: '/',
    Component: AppLayout,
    children: [
      { index: true, Component: HomePage },
      { path: 'login', lazy: { Component: async () => (await import('@/pages/LoginPage')).LoginPage } },
      { path: 'register', lazy: { Component: async () => (await import('@/pages/RegisterPage')).RegisterPage } },
      {
        path: 'forgot-password',
        lazy: { Component: async () => (await import('@/pages/ForgotPasswordPage')).ForgotPasswordPage },
      },
      {
        path: 'reset-password',
        lazy: { Component: async () => (await import('@/pages/ResetPasswordPage')).ResetPasswordPage },
      },
      { path: 'verify-email', lazy: { Component: async () => (await import('@/pages/VerifyEmailPage')).VerifyEmailPage } },
      { path: 'privacy', lazy: { Component: async () => (await import('@/pages/PrivacyPage')).PrivacyPage } },
      { path: 'catalog', lazy: { Component: async () => (await import('@/pages/CatalogPage')).CatalogPage } },
      { path: 'equipment/:id', lazy: { Component: async () => (await import('@/pages/EquipmentPage')).EquipmentPage } },
      { path: 'users/:id', lazy: { Component: async () => (await import('@/pages/PublicProfilePage')).PublicProfilePage } },
      protectedRoute('profile', async () => (await import('@/pages/ProfilePage')).ProfilePage),
      protectedRoute('my/listings', async () => (await import('@/pages/MyListingsPage')).MyListingsPage),
      protectedRoute('my/listings/new', async () => (await import('@/pages/ListingCreatePage')).ListingCreatePage),
      protectedRoute('my/bookings', async () => (await import('@/pages/MyBookingsPage')).MyBookingsPage),
      protectedRoute('my/requests', async () => (await import('@/pages/IncomingBookingsPage')).IncomingBookingsPage),
      protectedRoute('my/listings/:id/edit', async () => (await import('@/pages/ListingEditPage')).ListingEditPage),
      protectedRoute('messages/:id?', async () => (await import('@/pages/MessagesPage')).MessagesPage),
      protectedRoute('admin/:tab?', async () => (await import('@/pages/AdminPage')).AdminPage, ['ADMIN']),
      { path: '*', Component: NotFoundPage },
    ],
  },
])
