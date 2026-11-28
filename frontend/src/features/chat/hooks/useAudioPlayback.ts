import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Play / pause / seek for a hidden {@code <audio>} element, with the position updated every animation frame while
 * playing. Starting one voice message pauses any other audio on the page.
 */
export function useAudioPlayback() {
  const audio = useRef<HTMLAudioElement>(null)
  const [playing, setPlaying] = useState(false)
  const [position, setPosition] = useState(0)

  useEffect(() => {
    const element = audio.current
    if (!element) return
    let frame = 0
    const tick = () => {
      setPosition(element.currentTime)
      frame = requestAnimationFrame(tick)
    }
    const onPlay = () => {
      document.querySelectorAll('audio').forEach((other) => other !== element && other.pause())
      setPlaying(true)
      frame = requestAnimationFrame(tick)
    }
    const onStop = () => {
      setPlaying(false)
      cancelAnimationFrame(frame)
      setPosition(element.ended ? 0 : element.currentTime)
    }
    element.addEventListener('play', onPlay)
    element.addEventListener('pause', onStop)
    element.addEventListener('ended', onStop)
    return () => {
      cancelAnimationFrame(frame)
      element.removeEventListener('play', onPlay)
      element.removeEventListener('pause', onStop)
      element.removeEventListener('ended', onStop)
    }
  }, [])

  const toggle = useCallback(() => {
    const element = audio.current
    if (!element) return
    if (element.paused) void element.play()
    else element.pause()
  }, [])

  const seek = useCallback((seconds: number) => {
    const element = audio.current
    if (!element) return
    element.currentTime = seconds
    setPosition(seconds)
  }, [])

  return { audio, playing, position, toggle, seek }
}
