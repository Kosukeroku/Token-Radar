export interface Coin {
    id: string;
    symbol: string;
    name: string;
    imageUrl: string;
    currentPrice: number;
    lastUpdated: string;
    priceChange24h: number;
    priceChangePercentage24h: number;
    marketCapRank: number;
    marketCap: number;
    totalVolume: number;
    priceChangePercentage1h: number;
    priceChangePercentage7d: number;
    priceChangePercentage30d: number;
    sparklineData: string;
    high24h: number;
    low24h: number;
    ath: number;
    athChangePercentage: number;
    athDate: string;
    atl: number;
    atlChangePercentage: number;
    atlDate: string;
    circulatingSupply: number;
}

export interface TrackedCurrency {
    id: number;
    coinId: string;
    coinName: string;
    coinSymbol: string;
    coinImageUrl: string;
    addedAt: string;
    currentPrice: number;
    priceChangePercentage24h: number;
    marketCapRank: number;
    marketCap: number;
    totalVolume: number;
}

export interface UserProfile {
    id: number;
    username: string;
    email: string;
    createdAt: string;
    trackedCurrencies: TrackedCurrency[];
}

export interface ApiResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

export interface AuthResponse {
    token: string;
    type: string;
    id: number;
    username: string;
    email: string;
}