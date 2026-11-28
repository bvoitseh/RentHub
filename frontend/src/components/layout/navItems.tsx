import type { ComponentType, ReactNode } from 'react'
import { ChatUnreadBadge } from '@/features/chat/components/ChatUnreadBadge'
import type { Role } from '@/features/auth/types'
import { uk } from '@/i18n/uk'

export interface NavItem {
  to: string
  label: string
  end?: boolean
  /** Only these roles see the item; absent = everyone, guests included. */
  roles?: Role[]
  icon: ReactNode
  /** Rendered after the label, e.g. an unread counter. */
  badge?: ComponentType
}

const icon = (d: string) => (
  <svg
    className="h-5 w-5 shrink-0"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="1.8"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d={d} />
  </svg>
)

/** Main navigation (D-53), shared by the desktop sidebar and the phone drawer. */
export const navItems: NavItem[] = [
  { to: '/', end: true, label: uk.nav.home, icon: icon('M3 11l9-7 9 7M5 10v10h5v-6h4v6h5V10') },
  { to: '/catalog', label: uk.catalog.nav, icon: icon('M4 5h7v7H4zM13 5h7v7h-7zM4 14h7v6H4zM13 14h7v6h-7z') },
  {
    to: '/my/bookings',
    label: uk.booking.nav.mine,
    roles: ['USER'],
    icon: icon('M4 7h16v13H4zM8 3v4M16 3v4M4 11h16'),
  },
  {
    to: '/my/listings',
    label: uk.listings.nav,
    roles: ['USER'],
    icon: icon('M3 12l9-9h8v8l-9 9zM15.5 8.5h.01'),
  },
  {
    to: '/my/requests',
    label: uk.booking.nav.incoming,
    roles: ['USER'],
    icon: icon('M4 4h16v12H8l-4 4zM8 9h8M8 12h5'),
  },
  {
    to: '/messages',
    label: uk.chat.nav,
    roles: ['USER'],
    icon: icon('M4 5h16v11H9l-5 4zM8 9h8M8 12h6'),
    badge: ChatUnreadBadge,
  },
  { to: '/admin', label: uk.admin.nav, roles: ['ADMIN'], icon: icon('M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z') },
]
