import { useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router'
import { SideNav } from '@/components/layout/SideNav'
import { uk } from '@/i18n/uk'

/**
 * Phone navigation (D-53): ☰ opens a left drawer built on a modal <dialog> (focus trap, Escape, focus back to ☰).
 * It closes on a link, the backdrop, Escape or a route change; page scrolling is locked while it is open.
 */
export function MobileNav() {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDialogElement>(null)
  const { pathname } = useLocation()
  const [shownAt, setShownAt] = useState(pathname)

  if (open && shownAt !== pathname) setOpen(false)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
    document.body.style.overflow = open ? 'hidden' : ''
    return () => {
      document.body.style.overflow = ''
    }
  }, [open])

  const show = () => {
    setShownAt(pathname)
    setOpen(true)
  }

  return (
    <>
      <button
        type="button"
        onClick={show}
        aria-label={uk.nav.openMenu}
        aria-expanded={open}
        aria-controls="mobile-nav"
        className="-ml-2 rounded-md p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900 lg:hidden"
      >
        <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
          <path strokeLinecap="round" d="M4 7h16M4 12h16M4 17h16" />
        </svg>
      </button>
      <dialog
        id="mobile-nav"
        ref={ref}
        aria-label={uk.nav.main}
        onClose={() => setOpen(false)}
        onClick={(event) => {
          if (event.target === ref.current) setOpen(false)
        }}
        className="m-0 h-full max-h-none w-72 max-w-[85vw] bg-surface p-0 text-slate-900 shadow-xl backdrop:bg-slate-950/50"
      >
        <div className="flex h-full flex-col gap-4 px-3 py-4">
          <div className="flex items-center justify-between px-3">
            <span className="text-lg font-bold text-brand-700">{uk.app.name}</span>
            <button
              type="button"
              onClick={() => setOpen(false)}
              aria-label={uk.nav.closeMenu}
              className="rounded-md p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                <path strokeLinecap="round" d="M6 6l12 12M18 6L6 18" />
              </svg>
            </button>
          </div>
          <SideNav onNavigate={() => setOpen(false)} />
        </div>
      </dialog>
    </>
  )
}
