export interface Coin {
    id: string;
    symbol: string;
    name: string;
    imageUrl: string;
    currentPrice: number;
    priceChange24h: number;
    priceChangePercentage24h: number;
    marketCapRank: number;
    marketCap: number;
    totalVolume: number;
}

export interface CoinPage {
    content: Coin[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}