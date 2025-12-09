import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { PriceChangeBadge } from "@/components/PriceChangeBadge"

interface PriceChangesProps {
    coin: {
        priceChangePercentage1h: number
        priceChangePercentage24h: number
        priceChangePercentage7d: number
        priceChangePercentage30d: number
    }
}

export function PriceChanges({ coin }: PriceChangesProps) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Price Changes</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">1h</span>
                        <PriceChangeBadge value={coin.priceChangePercentage1h} />
                    </div>
                    <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">24h</span>
                        <PriceChangeBadge value={coin.priceChangePercentage24h} />
                    </div>
                    <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">7d</span>
                        <PriceChangeBadge value={coin.priceChangePercentage7d} />
                    </div>
                    <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">30d</span>
                        <PriceChangeBadge value={coin.priceChangePercentage30d} />
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}