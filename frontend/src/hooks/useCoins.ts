import { useQuery } from '@tanstack/react-query'
import { api } from '../services/api'
import type { Coin } from '../types/coin'

interface ApiResponse {
    content: Coin[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

export const useCoins = (page: number) => {
    return useQuery({
        queryKey: ['coins', page],
        queryFn: async (): Promise<{ coins: Coin[]; totalPages: number }> => {
            const response = await api.get<ApiResponse>(`/coins/dashboard?page=${page}&size=20`)
            return {
                coins: response.data.content,
                totalPages: response.data.totalPages
            }
        },
        staleTime: 5 * 60 * 1000, // 5 min
        retry: 2,
    })
}