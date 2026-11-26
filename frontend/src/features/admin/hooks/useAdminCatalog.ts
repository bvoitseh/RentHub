import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminCatalogApi } from '@/features/admin/api/adminCatalogApi'
import type { CategoryPayload } from '@/features/admin/catalogTypes'

export function useAdminCategories() {
  return useQuery({ queryKey: ['admin', 'categories'], queryFn: ({ signal }) => adminCatalogApi.categories(signal) })
}

/** Category changes show in the admin list, the filters, the listing form and the catalogue. */
function useCategoryRefresh() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: ['admin'] })
    void queryClient.invalidateQueries({ queryKey: ['catalog'] })
  }
}

export function useSaveCategory(id: number | null) {
  const onSuccess = useCategoryRefresh()
  return useMutation({
    mutationFn: (payload: CategoryPayload) =>
      id === null ? adminCatalogApi.createCategory(payload) : adminCatalogApi.updateCategory(id, payload),
    onSuccess,
  })
}

export function useDeleteCategory(id: number) {
  const onSuccess = useCategoryRefresh()
  return useMutation({ mutationFn: (targetId?: number) => adminCatalogApi.deleteCategory(id, targetId), onSuccess })
}

export function useCommission() {
  return useQuery({ queryKey: ['admin', 'commission'], queryFn: ({ signal }) => adminCatalogApi.commission(signal) })
}

export function useChangeCommission() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (rate: number) => adminCatalogApi.changeCommission(rate),
    onSuccess: (data) => queryClient.setQueryData(['admin', 'commission'], data),
  })
}

export function useStatistics(from: string, to: string) {
  return useQuery({
    queryKey: ['admin', 'statistics', from, to],
    queryFn: ({ signal }) => adminCatalogApi.statistics(from, to, signal),
    enabled: from !== '' && to !== '' && from <= to,
  })
}
