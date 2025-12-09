import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { formatPriceDetailed, formatDate, timeAgo } from "@/utils/formatters"
import { PriceChangeBadge } from "@/components/PriceChangeBadge"

interface AllTimeHighProps {
    coin: {
        ath: number
        athDate: string
        athChangePercentage: number
    }
}

export function AllTimeHigh({ coin }: AllTimeHighProps) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>All-Time High</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <div>
                        <div className="flex justify-between items-center mb-2">
                            <span className="text-muted-foreground">Price</span>
                            <span className="font-semibold">{formatPriceDetailed(coin.ath)}</span>
                        </div>
                        <div className="flex justify-between items-center text-sm">
              <span className="text-muted-foreground">
                {formatDate(coin.athDate)}
                  {coin.athDate && (
                      <span className="ml-1">
                    ({timeAgo(coin.athDate)} ago)
                  </span>
                  )}
              </span>
                            <PriceChangeBadge value={coin.athChangePercentage} />
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}