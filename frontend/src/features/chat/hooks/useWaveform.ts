import { useEffect, useState } from 'react'

/** Bars drawn for a voice message. */
export const WAVEFORM_BARS = 40

export interface Waveform {
  /** Loudness per bar, 0…1. */
  peaks: number[]
  /** Exact length in seconds (browser-recorded WebM files often report no duration in their metadata). */
  duration: number
}

const cache = new Map<string, Promise<Waveform>>()

async function decode(url: string): Promise<Waveform> {
  const response = await fetch(url, { credentials: 'same-origin' })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  const context = new AudioContext()
  try {
    const audio = await context.decodeAudioData(await response.arrayBuffer())
    const samples = audio.getChannelData(0)
    const step = Math.max(1, Math.floor(samples.length / WAVEFORM_BARS))
    const raw: number[] = []
    for (let bar = 0; bar < WAVEFORM_BARS; bar++) {
      let peak = 0
      for (let i = bar * step; i < Math.min((bar + 1) * step, samples.length); i++) {
        peak = Math.max(peak, Math.abs(samples[i] ?? 0))
      }
      raw.push(peak)
    }
    const max = Math.max(...raw, 0.01)
    return { peaks: raw.map((p) => p / max), duration: audio.duration }
  } finally {
    void context.close()
  }
}

/** Decodes the voice message once per URL (the browser caches the file) and returns its waveform; null while loading. */
export function useWaveform(url: string) {
  const [waveform, setWaveform] = useState<Waveform | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    let pending = cache.get(url)
    if (!pending) {
      pending = decode(url)
      cache.set(url, pending)
      pending.catch(() => cache.delete(url))
    }
    pending.then(
      (result) => active && setWaveform(result),
      () => active && setFailed(true),
    )
    return () => {
      active = false
    }
  }, [url])

  return { waveform, failed }
}
