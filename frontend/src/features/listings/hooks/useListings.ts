import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { catalogKeys } from '@/features/catalog/hooks/useCatalogQueries'
import type { EquipmentDetails, Photo } from '@/features/catalog/types'
import { listingsApi } from '@/features/listings/api/listingsApi'
import type { ListingPayload } from '@/features/listings/types'

export const myListingsKey = ['listings', 'mine'] as const

export function useMyListings() {
  return useQuery({ queryKey: myListingsKey, queryFn: ({ signal }) => listingsApi.mine(signal) })
}

/** After any change: the card gets the fresh data, the dashboard and catalogue searches are refetched. */
function useSync() {
  const queryClient = useQueryClient()
  return {
    card: (item: EquipmentDetails) => queryClient.setQueryData(catalogKeys.card(item.id), item),
    photos: (id: number, photos: Photo[]) =>
      queryClient.setQueryData<EquipmentDetails>(catalogKeys.card(id), (old) => (old ? { ...old, photos } : old)),
    lists: () => {
      void queryClient.invalidateQueries({ queryKey: myListingsKey })
      void queryClient.invalidateQueries({ queryKey: ['catalog', 'search'] })
    },
  }
}

export function useSaveListing(id?: number) {
  const sync = useSync()
  return useMutation({
    mutationFn: (payload: ListingPayload) => (id ? listingsApi.update(id, payload) : listingsApi.create(payload)),
    onSuccess: (item) => {
      sync.card(item)
      sync.lists()
    },
  })
}

export function useArchiveListing() {
  const sync = useSync()
  return useMutation({
    mutationFn: listingsApi.archive,
    onSuccess: (item) => {
      sync.card(item)
      sync.lists()
    },
  })
}

export function useDeleteListing() {
  const sync = useSync()
  return useMutation({ mutationFn: listingsApi.remove, onSuccess: sync.lists })
}

export function usePhotoMutations(id: number) {
  const sync = useSync()
  const onSuccess = (photos: Photo[]) => {
    sync.photos(id, photos)
    sync.lists()
  }
  return {
    upload: useMutation({ mutationFn: (file: File) => listingsApi.uploadPhoto(id, file), onSuccess }),
    remove: useMutation({ mutationFn: (photoId: number) => listingsApi.removePhoto(id, photoId), onSuccess }),
  }
}
