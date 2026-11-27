import { API_BASE } from '@/api/client'
import { CSRF_HEADER, readCsrfToken } from '@/api/csrf'
import { ProblemError, type ProblemDetail } from '@/api/problem'

/**
 * Multipart upload of one file with progress (fetch cannot report upload progress): same-origin cookies, CSRF
 * header, and a non-2xx answer turned into {@link ProblemError} like the rest of the API client.
 */
function parse(text: string): unknown {
  try {
    return text ? JSON.parse(text) : null
  } catch {
    return null
  }
}

export function uploadWithProgress<T>(path: string, file: File, onProgress: (percent: number) => void): Promise<T> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('POST', `${API_BASE}/${path}`)
    xhr.setRequestHeader('Accept', 'application/json')
    const token = readCsrfToken()
    if (token) xhr.setRequestHeader(CSRF_HEADER, token)
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100))
    }
    xhr.onerror = () => reject(new TypeError('Network error'))
    xhr.onload = () => {
      const body = parse(xhr.responseText)
      if (xhr.status >= 200 && xhr.status < 300) resolve(body as T)
      else {
        const problem = (body ?? {}) as Partial<ProblemDetail>
        reject(
          new ProblemError({ ...problem, status: problem.status ?? xhr.status, code: problem.code ?? 'http_error' }),
        )
      }
    }
    const form = new FormData()
    form.append('file', file)
    xhr.send(form)
  })
}
