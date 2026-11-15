import { uk } from '@/i18n/uk'

/** RFC 9457 problem details as produced by the backend (see ApiExceptionHandler). */
export interface FieldViolation {
  field: string
  message: string
}

export interface ProblemDetail {
  type?: string
  title?: string
  status: number
  detail?: string
  instance?: string
  code: string
  errors?: FieldViolation[]
  [extra: string]: unknown
}

/** Error thrown by the API client for any non-2xx response; carries the parsed problem. */
export class ProblemError extends Error {
  readonly status: number
  readonly code: string
  readonly problem: ProblemDetail

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `HTTP ${problem.status}`)
    this.name = 'ProblemError'
    this.status = problem.status
    this.code = problem.code
    this.problem = problem
  }
}

export function isProblem(error: unknown): error is ProblemError {
  return error instanceof ProblemError
}

/** Ukrainian text to show the user for any failure (backend detail when present, generic text otherwise). */
export function problemMessage(error: unknown): string {
  if (isProblem(error)) return error.problem.detail ?? uk.common.errorGeneric
  if (error instanceof TypeError) return uk.common.errorNetwork
  return uk.common.errorGeneric
}
