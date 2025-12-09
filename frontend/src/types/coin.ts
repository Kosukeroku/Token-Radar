export interface Coin {
    id: string;
    name: string;
    symbol: string;
    imageUrl: string;
    currentPrice: number;
    priceChangePercentage24h: number;
    marketCapRank: number;
    marketCap: number;
    totalVolume: number;
}

export interface CoinDetail extends Coin {
    priceChange24h: number;
    priceChangePercentage1h: number;
    priceChangePercentage7d: number;
    priceChangePercentage30d: number;
    high24h: number;
    low24h: number;
    ath: number;
    athChangePercentage: number;
    athDate: string;
    atl: number;
    atlChangePercentage: number;
    atlDate: string;
    circulatingSupply: number;
    sparklineData: string;
    lastUpdated: string;
}

export interface CoinPage {
    content: CoinDetail[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
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
    trackedCount: number;
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

export interface TrackButtonProps {
    coinId: string;
    coinName?: string;
    size?: "sm" | "default" | "lg" | "icon";
    variant?: "default" | "outline" | "secondary" | "ghost";
    className?: string;
    showText?: boolean;
    tableStyle?: boolean;
    onTrackChange?: (isTracked: boolean) => void;
}