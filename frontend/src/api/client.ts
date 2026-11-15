import { CSRF_HEADER, readCsrfToken } from '@/api/csrf'
import { ProblemError, type ProblemDetail } from '@/api/problem'

export const API_BASE = '/api/v1'

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export interface RequestOptions {
  method?: Method
  body?: unknown
  query?: Record<string, string | number | boolean | undefined | null>
  signal?: AbortSignal
}

function buildUrl(path: string, query?: RequestOptions['query']): string {
  const url = path.startsWith('/') ? path : `${API_BASE}/${path}`
  if (!query) return url
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') params.set(key, String(value))
  }
  const qs = params.toString()
  return qs ? `${url}?${qs}` : url
}

async function parseProblem(response: Response): Promise<ProblemDetail> {
  const fallback: ProblemDetail = { status: response.status, code: 'http_error' }
  try {
    const data = (await response.json()) as Partial<ProblemDetail>
    return { ...fallback, ...data, status: data.status ?? response.status, code: data.code ?? fallback.code }
  } catch {
    return fallback
  }
}

/**
 * Thin fetch wrapper: same-origin cookies, JSON bodies, CSRF header on mutating requests,
 * and non-2xx responses turned into {@link ProblemError}. `path` is relative to /api/v1 unless it starts with "/".
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, query, signal } = options
  const headers: Record<string, string> = { Accept: 'application/json' }
  const isForm = body instanceof FormData
  if (body !== undefined && !isForm) headers['Content-Type'] = 'application/json'
  if (method !== 'GET') {
    const token = readCsrfToken()
    if (token) headers[CSRF_HEADER] = token
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers,
    credentials: 'same-origin',
    body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
    signal,
  })

  if (!response.ok) throw new ProblemError(await parseProblem(response))
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

export const api = {
  get: <T>(path: string, query?: RequestOptions['query'], signal?: AbortSignal) =>
    request<T>(path, { query, signal }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
  put: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body }),
  delete: <T = void>(path: string) => request<T>(path, { method: 'DELETE' }),
}
