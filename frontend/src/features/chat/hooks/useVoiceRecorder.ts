import { useCallback, useEffect, useRef, useState } from 'react'

/** Longest voice message (D-57). */
export const MAX_VOICE_SECONDS = 300

const TYPES: [string, string][] = [
  ['audio/webm;codecs=opus', 'webm'],
  ['audio/ogg;codecs=opus', 'ogg'],
  ['audio/mp4', 'm4a'],
]

export type RecorderError = 'denied' | 'notFound' | 'unsupported' | null

/**
 * Records a voice message with the browser's MediaRecorder (no library). `stop()` resolves with the file, `cancel()`
 * throws the recording away; recording stops by itself after {@link MAX_VOICE_SECONDS} and `onLimit` gets the file.
 */
export function useVoiceRecorder(onLimit: (file: File) => void) {
  const [seconds, setSeconds] = useState<number | null>(null)
  const [error, setError] = useState<RecorderError>(null)
  const recorder = useRef<MediaRecorder | null>(null)
  const resolveStop = useRef<((file: File | null) => void) | null>(null)

  const release = useCallback(() => {
    recorder.current?.stream.getTracks().forEach((track) => track.stop())
    recorder.current = null
    setSeconds(null)
  }, [])

  const start = useCallback(async () => {
    setError(null)
    const supported = TYPES.find(([type]) => typeof MediaRecorder !== 'undefined' && MediaRecorder.isTypeSupported(type))
    if (!supported || !navigator.mediaDevices?.getUserMedia) {
      setError('unsupported')
      return
    }
    let stream: MediaStream
    try {
      stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    } catch (cause) {
      setError(cause instanceof DOMException && cause.name === 'NotFoundError' ? 'notFound' : 'denied')
      return
    }
    const [type, extension] = supported
    const chunks: Blob[] = []
    const media = new MediaRecorder(stream, { mimeType: type })
    media.ondataavailable = (event) => chunks.push(event.data)
    media.onstop = () => {
      const file = new File(chunks, `voice.${extension}`, { type: type.split(';')[0] })
      const resolve = resolveStop.current
      resolveStop.current = null
      release()
      if (resolve) resolve(file)
      else onLimit(file)
    }
    recorder.current = media
    media.start()
    setSeconds(0)
  }, [onLimit, release])

  const stop = useCallback(
    () =>
      new Promise<File | null>((resolve) => {
        if (!recorder.current) return resolve(null)
        resolveStop.current = resolve
        recorder.current.stop()
      }),
    [],
  )

  const cancel = useCallback(() => {
    if (!recorder.current) return
    resolveStop.current = () => undefined
    recorder.current.stop()
  }, [])

  useEffect(() => {
    if (seconds === null) return
    if (seconds >= MAX_VOICE_SECONDS) {
      recorder.current?.stop()
      return
    }
    const timer = window.setTimeout(() => setSeconds((s) => (s === null ? s : s + 1)), 1000)
    return () => window.clearTimeout(timer)
  }, [seconds])

  useEffect(() => () => recorder.current?.stream.getTracks().forEach((track) => track.stop()), [])

  return { recording: seconds !== null, seconds: seconds ?? 0, error, start, stop, cancel }
}
