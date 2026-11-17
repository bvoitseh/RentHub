import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { isProblem } from '@/api/problem'

/**
 * Copies backend field violations (`errors: [{field, message}]`) onto the form.
 * Returns true when at least one violation matched a form field, so the caller can skip the generic alert.
 */
export function applyServerErrors<T extends FieldValues>(error: unknown, setError: UseFormSetError<T>): boolean {
  if (!isProblem(error) || !error.problem.errors?.length) return false
  let applied = false
  for (const violation of error.problem.errors) {
    setError(violation.field as Path<T>, { type: 'server', message: violation.message })
    applied = true
  }
  return applied
}
