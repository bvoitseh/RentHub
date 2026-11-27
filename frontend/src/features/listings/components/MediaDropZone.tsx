import { useRef, useState } from 'react'

export interface MediaDropZoneProps {
  accept: string
  multiple?: boolean
  disabled?: boolean
  prompt: string
  action: string
  onFiles: (files: File[]) => void
}

/** Drop area with a «choose files» link; keyboard users focus the link (FR-4a, D-52). */
export function MediaDropZone({
  accept,
  multiple = false,
  disabled = false,
  prompt,
  action,
  onFiles,
}: MediaDropZoneProps) {
  const input = useRef<HTMLInputElement>(null)
  const [over, setOver] = useState(false)

  return (
    <div
      onDragOver={(event) => {
        if (disabled) return
        event.preventDefault()
        setOver(true)
      }}
      onDragLeave={() => setOver(false)}
      onDrop={(event) => {
        event.preventDefault()
        setOver(false)
        if (!disabled) onFiles(Array.from(event.dataTransfer.files))
      }}
      className={`rounded-md border-2 border-dashed px-4 py-5 text-center text-sm ${
        over ? 'border-brand-600 bg-brand-50' : 'border-slate-300'
      } ${disabled ? 'opacity-60' : ''}`}
    >
      <span className="text-slate-600">{prompt} </span>
      <button
        type="button"
        disabled={disabled}
        onClick={() => input.current?.click()}
        className="font-medium text-brand-700 hover:underline disabled:cursor-not-allowed disabled:no-underline"
      >
        {action}
      </button>
      <input
        ref={input}
        type="file"
        accept={accept}
        multiple={multiple}
        className="hidden"
        onChange={(event) => {
          const files = Array.from(event.target.files ?? [])
          event.target.value = ''
          if (files.length > 0) onFiles(files)
        }}
      />
    </div>
  )
}
