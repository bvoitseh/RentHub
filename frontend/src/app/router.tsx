import { createBrowserRouter } from 'react-router'
import { AppLayout } from '@/components/layout/AppLayout'
import { RequireAuth } from '@/features/auth/components/RequireAuth'
import { HomePage } from '@/pages/HomePage'
import { NotFoundPage } from '@/pages/NotFoundPage'

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
      {
        path: 'profile',
        lazy: {
          Component: async () => {
            const { ProfilePage } = await import('@/pages/ProfilePage')
            return function ProtectedProfilePage() {
              return (
                <RequireAuth>
                  <ProfilePage />
                </RequireAuth>
              )
            }
          },
        },
      },
      { path: '*', Component: NotFoundPage },
    ],
  },
])
