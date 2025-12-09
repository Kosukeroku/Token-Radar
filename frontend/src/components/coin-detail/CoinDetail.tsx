import { useParams, useNavigate } from "react-router-dom"
import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button.tsx"
import { Loader } from "@/components/Loader.tsx"
import { ErrorDisplay } from "@/components/ErrorDisplay.tsx"
import { CoinHeader } from "./CoinHeader"
import { CoinChart } from "./CoinChart"
import { MarketData } from "./MarketData"
import { PriceChanges } from "./PriceChanges"
import { AllTimeHigh } from "./AllTimeHigh"
import { MarketAnalytics } from "./MarketAnalytics"
import { useCoinDetail } from "@/hooks/useCoinDetail.ts"

export function CoinDetail() {
    const { coinId } = useParams<{ coinId: string }>()
    const navigate = useNavigate()
    const { data: coin, isLoading, error, refetch } = useCoinDetail(coinId || '')

    if (isLoading) return <Loader />

    if (error || !coin) {
        return (
            <div className="container mx-auto px-4 py-8">
                <ErrorDisplay
                    message={`Failed to load data for ${coinId}`}
                    onRetry={() => refetch()}
                />
            </div>
        )
    }

    return (
        <div className="container mx-auto px-4 py-8">
            {/* Back button */}
            <Button
                variant="ghost"
                onClick={() => navigate(-1)}
                className="mb-6 text-base h-11 px-5"
            >
                <ArrowLeft className="mr-2 h-5 w-5" />
                Back to Dashboard
            </Button>

            {/* coin header */}
            <CoinHeader coin={coin} />

            {/* main content */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
                {/* left column: chart & market data */}
                <div className="lg:col-span-2 space-y-6">
                    <CoinChart
                        sparklineData={coin.sparklineData}
                        isPositive={coin.priceChangePercentage24h >= 0}
                    />
                    <MarketData coin={coin} />
                </div>

                {/* right column: stats */}
                <div className="space-y-6">
                    <PriceChanges coin={coin} />
                    <AllTimeHigh coin={coin} />
                    <MarketAnalytics coin={coin} />
                </div>
            </div>
        </div>
    )
}