import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { formatPriceDetailed } from "@/utils/formatters"

interface MarketAnalyticsProps {
    coin: {
        totalVolume: number
        marketCap: number
        currentPrice: number
        ath: number
    }
}

export function MarketAnalytics({ coin }: MarketAnalyticsProps) {
    const volumeRatio = (coin.totalVolume / coin.marketCap) * 100
    const athPercentage = (coin.currentPrice / coin.ath) * 100

    // Определяем статус для Volume/MCap Ratio - ИЗМЕНЯЕМ ЦВЕТ НА КРАСНЫЙ
    const getVolumeStatus = (ratio: number) => {
        if (ratio > 10) return {
            text: 'High trading volume relative to market cap',
            color: 'text-green-600 dark:text-green-400',
            size: 'text-sm' // единый размер шрифта
        }
        if (ratio > 3.5) return {
            text: 'Healthy trading activity',
            color: 'text-yellow-600 dark:text-yellow-400',
            size: 'text-sm'
        }
        return {
            text: 'Moderate trading volume',
            color: 'text-red-600 dark:text-red-400',
            size: 'text-sm'
        }
    }

    // calculating status relative to ATH
    const getAthStatus = (percentage: number) => {
        if (percentage >= 90) return {
            text: "Near All-Time High",
            color: "text-green-600 dark:text-green-400",
            size: "text-sm"
        }
        if (percentage >= 55) return {
            text: "Mid-range from ATH",
            color: "text-yellow-600 dark:text-yellow-400",
            size: "text-sm"
        }
        return {
            text: "Far From ATH",
            color: "text-red-700 dark:text-red-500",
            size: "text-sm"
        }
    }

    const volumeStatus = getVolumeStatus(volumeRatio)
    const athStatus = getAthStatus(athPercentage)

    // color for volume/mcap ratio
    const getVolumeRatioColor = (ratio: number) => {
        if (ratio > 10) return 'text-green-600 dark:text-green-400'
        if (ratio > 5) return 'text-yellow-600 dark:text-yellow-400'
        return 'text-red-600 dark:text-red-400' // МЕНЯЕМ НА КРАСНЫЙ
    }

    const volumeRatioColor = getVolumeRatioColor(volumeRatio)

    return (
        <Card>
            <CardHeader>
                <CardTitle>Market Analytics</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                {/* volume/market cap ratio */}
                <div className="p-4 bg-accent/30 rounded-lg border">
                    <div className="flex justify-between items-center mb-2">
                        <span className="text-muted-foreground">Volume/MCap Ratio</span>
                        <span className={`font-bold text-lg ${volumeRatioColor}`}>
              {volumeRatio.toFixed(2)}%
            </span>
                    </div>
                    <div className={`font-medium ${volumeStatus.color} ${volumeStatus.size}`}>
                        {volumeStatus.text}
                    </div>
                </div>

                {/* current vs ATH */}
                <div className="p-4 bg-accent/30 rounded-lg border space-y-3">
                    <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">Current vs ATH</span>
                        <span className={`font-bold text-lg ${athStatus.color}`}>
              {athPercentage.toFixed(1)}%
            </span>
                    </div>

                    {/* progress bar */}
                    <Progress value={athPercentage} className="h-2" />

                    {/* labels under progress bar */}
                    <div className="flex justify-between items-center text-xs pt-1">
                        {/* left block: 0% + status */}
                        <div className="flex items-center gap-2">
                            <span className="text-muted-foreground">0%</span>
                            <span className={`font-medium ${athStatus.color} ${athStatus.size}`}>
                {athStatus.text}
              </span>
                        </div>

                        {/* right block: ATH price */}
                        <span className="text-muted-foreground">
              ATH: {formatPriceDetailed(coin.ath)}
            </span>
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}