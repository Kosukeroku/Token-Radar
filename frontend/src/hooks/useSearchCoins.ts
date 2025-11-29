import { useQuery } from '@tanstack/react-query'
import { api } from '../services/api'
import type { Coin } from '../types/coin'

export const useSearchCoins = (query: string) => {
    return useQuery({
        queryKey: ['search', query],
        queryFn: async (): Promise<Coin[]> => {
            if (!query.trim()) return []

            const response = await api.get<Coin[]>(`/coins/search?query=${encodeURIComponent(query)}`)
            return response.data
        },
        enabled: !!query.trim(), // only if there is a query
        staleTime: 2 * 60 * 1000, // 2 min
        retry: 1,
    })
}