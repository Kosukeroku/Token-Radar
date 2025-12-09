import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { SparklineChart } from "@/components/SparklineChart"

interface CoinChartProps {
    sparklineData: string
    isPositive: boolean
}

export function CoinChart({ sparklineData, isPositive }: CoinChartProps) {
    const parsedData = parseSparklineData(sparklineData)

    return (
        <Card>
            <CardHeader>
                <CardTitle>7-Day Price Chart</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="h-72">
                    {parsedData.length > 0 ? (
                        <SparklineChart
                            data={parsedData}
                            isPositive={isPositive}
                            showAxes={true}
                        />
                    ) : (
                        <div className="flex items-center justify-center h-full text-muted-foreground">
                            No chart data available
                        </div>
                    )}
                </div>
            </CardContent>
        </Card>
    )
}

function parseSparklineData(sparklineData: string): number[] {
    try {
        const parsed = JSON.parse(sparklineData)
        return Array.isArray(parsed)
            ? parsed.filter((n): n is number => typeof n === 'number')
            : []
    } catch {
        return []
    }
}