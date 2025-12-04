import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';
import type { CoinDetail } from '../types/coin';

export const useCoinDetail = (coinId: string) => {
    return useQuery({
        queryKey: ['coin', coinId],
        queryFn: async (): Promise<CoinDetail> => {
            try {
                const response = await api.get<CoinDetail>(`/coins/${coinId}`);
                return response.data;
            } catch (error) {
                console.error(`Failed to fetch coin details for ${coinId}:`, error);
                throw new Error(`Failed to load ${coinId}. Please try again.`);
            }
        },
        enabled: !!coinId,
        staleTime: 2 * 60 * 1000, // 2 mins
        retry: 1,
        retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    });
};