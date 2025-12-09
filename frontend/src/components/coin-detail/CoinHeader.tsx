import { Badge } from "@/components/ui/badge"
import { CoinAvatar } from "@/components/CoinAvatar"
import { TrackButton } from "@/components/TrackButton"
import { formatPriceDetailed, formatDateTime } from "@/utils/formatters"
import { PriceChangeBadge } from "@/components/PriceChangeBadge"

interface CoinHeaderProps {
    coin: {
        id: string
        name: string
        symbol: string
        imageUrl: string
        currentPrice: number
        priceChangePercentage24h: number
        marketCapRank: number
        lastUpdated: string
    }
}

export function CoinHeader({ coin }: CoinHeaderProps) {
    return (
        <div className="bg-card rounded-xl border p-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                <div className="flex items-center gap-4">
                    <CoinAvatar
                        imageUrl={coin.imageUrl}
                        symbol={coin.symbol}
                        name={coin.name}
                        size="xl"
                    />
                    <div>
                        <div className="flex items-center gap-3">
                            <h1 className="text-3xl font-bold">{coin.name}</h1>
                            <TrackButton
                                coinId={coin.id}
                                coinName={coin.name}
                                size="default"
                                showText={true}
                            />
                        </div>
                        <div className="flex items-center gap-2 mt-2">
              <span className="text-muted-foreground text-lg">
                {coin.symbol.toUpperCase()}
              </span>
                            <Badge variant="secondary">
                                Rank #{coin.marketCapRank}
                            </Badge>
                        </div>
                    </div>
                </div>

                <div className="text-right">
                    <div className="text-3xl font-bold">
                        {formatPriceDetailed(coin.currentPrice)}
                    </div>
                    <div className="mt-2">
                        <PriceChangeBadge
                            value={coin.priceChangePercentage24h}
                            className="text-lg"
                        />
                    </div>
                    <div className="text-sm text-muted-foreground mt-2">
                        Updated: {formatDateTime(coin.lastUpdated)}
                    </div>
                </div>
            </div>
        </div>
    )
}