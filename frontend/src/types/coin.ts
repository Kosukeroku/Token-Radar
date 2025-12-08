export interface CoinDetail {
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

export interface Coin extends CoinDetail {}

export interface CoinPage {
    content: CoinDetail[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}