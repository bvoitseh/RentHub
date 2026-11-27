import { SideNav } from '@/components/layout/SideNav'

/** Desktop navigation column (D-53): sticky under the header, hidden on narrow screens (the drawer takes over). */
export function Sidebar() {
  return (
    <aside className="sticky top-14 hidden h-[calc(100vh-3.5rem)] w-56 shrink-0 overflow-y-auto border-r border-slate-200 bg-surface px-3 py-6 lg:block">
      <SideNav />
    </aside>
  )
}
