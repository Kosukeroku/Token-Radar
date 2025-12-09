import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { formatMarketCap, formatNumber, formatPriceDetailed } from "@/utils/formatters"

interface MarketDataProps {
    coin: {
        marketCap: number
        totalVolume: number
        circulatingSupply: number
        high24h: number
        low24h: number
    }
}

export function MarketData({ coin }: MarketDataProps) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Market Data</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-4">
                        <div className="flex justify-between items-center">
                            <span className="text-muted-foreground">Market Cap</span>
                            <span className="font-semibold">{formatMarketCap(coin.marketCap)}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-muted-foreground">24h Volume</span>
                            <span className="font-semibold">{formatMarketCap(coin.totalVolume)}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-muted-foreground">Circulating Supply</span>
                            <span className="font-semibold">{formatNumber(coin.circulatingSupply)}</span>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <div className="flex justify-between items-center">
                            <span className="text-muted-foreground">24h High</span>
                            <span className="font-semibold">{formatPriceDetailed(coin.high24h)}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-muted-foreground">24h Low</span>
                            <span className="font-semibold">{formatPriceDetailed(coin.low24h)}</span>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}