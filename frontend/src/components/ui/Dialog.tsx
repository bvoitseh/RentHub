import { useEffect, useRef, type ReactNode } from 'react'

export interface DialogProps {
  open: boolean
  title: string
  onClose: () => void
  children: ReactNode
}

/** Modal built on the native <dialog> element (focus trap, Esc to close, backdrop). */
export function Dialog({ open, title, onClose, children }: DialogProps) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      onClose={onClose}
      onClick={(event) => {
        if (event.target === ref.current) onClose()
      }}
      className="m-auto w-full max-w-md rounded-lg p-0 shadow-xl backdrop:bg-slate-900/50"
    >
      <div className="p-6" onClick={(event) => event.stopPropagation()}>
        <h2 className="mb-3 text-lg font-semibold text-slate-900">{title}</h2>
        {children}
      </div>
    </dialog>
  )
}
