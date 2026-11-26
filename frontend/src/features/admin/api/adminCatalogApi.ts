import { api } from '@/api/client'
import type { AdminCategory, CategoryPayload, Commission, Statistics } from '@/features/admin/catalogTypes'

/** FR-22, FR-23: categories, commission and statistics (ADMIN only on the server). */
export const adminCatalogApi = {
  categories: (signal?: AbortSignal) => api.get<AdminCategory[]>('admin/categories', undefined, signal),
  createCategory: (payload: CategoryPayload) => api.post<AdminCategory>('admin/categories', payload),
  updateCategory: (id: number, payload: CategoryPayload) => api.put<AdminCategory>(`admin/categories/${id}`, payload),
  deleteCategory: (id: number, targetId?: number) =>
    api.delete(targetId ? `admin/categories/${id}?targetId=${targetId}` : `admin/categories/${id}`),
  commission: (signal?: AbortSignal) => api.get<Commission>('admin/settings', undefined, signal),
  changeCommission: (rate: number) => api.put<Commission>('admin/settings', { rate }),
  statistics: (from: string, to: string, signal?: AbortSignal) =>
    api.get<Statistics>('admin/statistics', { from, to }, signal),
}
