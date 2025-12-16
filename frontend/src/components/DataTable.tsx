import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { CoinAvatar } from "./CoinAvatar"
import { PriceChangeBadge } from "./PriceChangeBadge"
import { TrackButton } from "./TrackButton"
import {formatPrice, formatMarketCap, formatVolume} from "@/utils/formatters"
import { Skeleton } from "@/components/ui/skeleton"
import { Search } from "lucide-react"
import type { Coin } from "@/types/coin"
import { AlertInputCell } from "./AlertInputCell"

interface DataTableProps {
    coins: Coin[]
    onRowClick?: (coin: Coin) => void
    showActions?: boolean
    onUntrack?: (coinId: string) => void
    showAddedDate?: boolean
    addedDates?: { [coinId: string]: string }
    showTrackButton?: boolean
    isLoading?: boolean
    dashboardMode?: boolean
}

export function DataTable({
                              coins,
                              onRowClick,
                              showActions = false,
                              onUntrack,
                              showAddedDate = false,
                              addedDates = {},
                              showTrackButton = false,
                              isLoading = false,
                              dashboardMode = false,
                          }: DataTableProps) {
    const hasActionsColumn = showTrackButton || (showActions && onUntrack !== undefined)

    if (isLoading) {
        return <DataTableSkeleton
            showAddedDate={showAddedDate}
            hasActionsColumn={hasActionsColumn}
            showAlerts={showActions}
            dashboardMode={dashboardMode}
        />
    }

    if (!coins || coins.length === 0) {
        return (
            <div className="rounded-lg border p-12 text-center">
                <div className="mb-4">
                    <Search className="h-12 w-12 mx-auto text-muted-foreground" />
                </div>
                <h3 className="text-xl font-semibold mb-2">No coins found</h3>
                <p className="text-muted-foreground">
                    Try adjusting your search or filter
                </p>
            </div>
        )
    }

    return (
        <div className="rounded-lg border overflow-hidden">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-12 text-center">#</TableHead>
                        {hasActionsColumn && <TableHead className="w-20"></TableHead>}
                        <TableHead className="min-w-[220px]">Coin</TableHead>
                        <TableHead className="text-right min-w-[120px]">Price</TableHead>
                        <TableHead className="text-right min-w-[120px]">24h Change</TableHead>

                        {/* dashboard columns */}
                        {dashboardMode && (
                            <>
                                <TableHead className="text-right min-w-[140px]">Market Cap</TableHead>
                                <TableHead className="text-right min-w-[140px]">Volume (24h)</TableHead>
                            </>
                        )}

                        {/* profile columns */}
                        {!dashboardMode && showActions && (
                            <>
                                <TableHead className="text-center min-w-[200px]">Alert (% change)</TableHead>
                                <TableHead className="text-center min-w-[200px]">Alert (price change)</TableHead>
                            </>
                        )}

                        {showAddedDate && (
                            <TableHead className="text-right min-w-[100px]">Added</TableHead>
                        )}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {coins.map((coin) => (
                        <TableRow key={coin.id} className={onRowClick ? "cursor-pointer hover:bg-accent/50" : ""}>
                            <TableCell className="text-center text-muted-foreground font-medium">
                                {coin.marketCapRank}
                            </TableCell>

                            {hasActionsColumn && (
                                <TableCell className="p-2" onClick={(e) => e.stopPropagation()}>
                                    {showTrackButton ? (
                                        <TrackButton
                                            coinId={coin.id}
                                            coinName={coin.name}
                                            size="sm"
                                            showText={true}
                                            tableStyle={true}
                                        />
                                    ) : showActions && onUntrack ? (
                                        <TrackButton
                                            coinId={coin.id}
                                            coinName={coin.name}
                                            size="sm"
                                            showText={true}
                                            tableStyle={true}
                                            onTrackChange={(isTracked) => {
                                                if (!isTracked) {
                                                    onUntrack?.(coin.id)
                                                }
                                            }}
                                        />
                                    ) : null}
                                </TableCell>
                            )}

                            <TableCell className="p-3" onClick={() => onRowClick?.(coin)}>
                                <div className="flex items-center">
                                    <div className="w-8 mr-3 flex-shrink-0">
                                        <CoinAvatar
                                            imageUrl={coin.imageUrl}
                                            symbol={coin.symbol}
                                            name={coin.name}
                                            size="sm"
                                        />
                                    </div>
                                    <div className="flex flex-col min-w-0 flex-1">
                                        <span className="font-medium truncate">{coin.name}</span>
                                        <span className="text-sm text-muted-foreground">
                                            {coin.symbol.toUpperCase()}
                                        </span>
                                    </div>
                                </div>
                            </TableCell>

                            <TableCell className="text-right font-medium tabular-nums p-3" onClick={() => onRowClick?.(coin)}>
                                {formatPrice(coin.currentPrice)}
                            </TableCell>
                            <TableCell className="text-right p-3" onClick={() => onRowClick?.(coin)}>
                                <PriceChangeBadge value={coin.priceChangePercentage24h} />
                            </TableCell>

                            {/* dashboard cells */}
                            {dashboardMode && (
                                <>
                                    <TableCell className="text-right font-medium tabular-nums p-3" onClick={() => onRowClick?.(coin)}>
                                        {formatMarketCap(coin.marketCap)}
                                    </TableCell>
                                    <TableCell className="text-right font-medium tabular-nums p-3" onClick={() => onRowClick?.(coin)}>
                                        {formatVolume(coin.totalVolume)}
                                    </TableCell>
                                </>
                            )}

                            {/* profile cells */}
                            {!dashboardMode && showActions && (
                                <>
                                    <TableCell className="text-center p-2">
                                        <div className="flex items-center justify-center gap-2">
                                            <AlertInputCell
                                                coinId={coin.id}
                                                coinName={coin.name}
                                                currentPrice={coin.currentPrice}
                                                type="downPercentage"
                                            />
                                            <div className="text-muted-foreground text-xs">/</div>
                                            <AlertInputCell
                                                coinId={coin.id}
                                                coinName={coin.name}
                                                currentPrice={coin.currentPrice}
                                                type="upPercentage"
                                            />
                                        </div>
                                    </TableCell>
                                    <TableCell className="text-center p-2">
                                        <div className="flex items-center justify-center gap-2">
                                            <AlertInputCell
                                                coinId={coin.id}
                                                coinName={coin.name}
                                                currentPrice={coin.currentPrice}
                                                type="downPrice"
                                            />
                                            <div className="text-muted-foreground text-xs">/</div>
                                            <AlertInputCell
                                                coinId={coin.id}
                                                coinName={coin.name}
                                                currentPrice={coin.currentPrice}
                                                type="upPrice"
                                            />
                                        </div>
                                    </TableCell>
                                </>
                            )}

                            {showAddedDate && (
                                <TableCell className="text-right text-muted-foreground p-3" onClick={() => onRowClick?.(coin)}>
                                    {addedDates[coin.id] || "N/A"}
                                </TableCell>
                            )}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    )
}

interface DataTableSkeletonProps {
    showAddedDate?: boolean
    hasActionsColumn?: boolean
    showAlerts?: boolean
    dashboardMode?: boolean
}

function DataTableSkeleton({
                               showAddedDate,
                               hasActionsColumn,
                               showAlerts,
                               dashboardMode = false
                           }: DataTableSkeletonProps) {
    return (
        <div className="rounded-lg border overflow-hidden">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-12 text-center">#</TableHead>
                        {hasActionsColumn && <TableHead className="w-20"></TableHead>}
                        <TableHead className="min-w-[220px]">Coin</TableHead>
                        <TableHead className="text-right min-w-[120px]">Price</TableHead>
                        <TableHead className="text-right min-w-[120px]">24h Change</TableHead>

                        {/* dashboard skeleton */}
                        {dashboardMode && (
                            <>
                                <TableHead className="text-right min-w-[140px]">Market Cap</TableHead>
                                <TableHead className="text-right min-w-[140px]">Volume (24h)</TableHead>
                            </>
                        )}

                        {/* profile skeleton */}
                        {!dashboardMode && showAlerts && (
                            <>
                                <TableHead className="text-center min-w-[200px]">Alert (% change)</TableHead>
                                <TableHead className="text-center min-w-[200px]">Alert (Price)</TableHead>
                            </>
                        )}

                        {showAddedDate && <TableHead className="text-right min-w-[100px]">Added</TableHead>}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {[...Array(10)].map((_, i) => (
                        <TableRow key={i}>
                            <TableCell className="text-center">
                                <Skeleton className="h-4 w-6 mx-auto" />
                            </TableCell>
                            {hasActionsColumn && (
                                <TableCell>
                                    <Skeleton className="h-7 w-16 rounded" />
                                </TableCell>
                            )}
                            <TableCell>
                                <div className="flex items-center">
                                    <div className="w-8 mr-3 flex-shrink-0">
                                        <Skeleton className="h-8 w-8 rounded-full" />
                                    </div>
                                    <div className="space-y-1 flex-1">
                                        <Skeleton className="h-4 w-24" />
                                        <Skeleton className="h-3 w-12" />
                                    </div>
                                </div>
                            </TableCell>
                            <TableCell className="text-right">
                                <Skeleton className="h-4 w-16 ml-auto" />
                            </TableCell>
                            <TableCell className="text-right">
                                <Skeleton className="h-4 w-12 ml-auto" />
                            </TableCell>

                            {/* dashboard skeleton */}
                            {dashboardMode && (
                                <>
                                    <TableCell className="text-right">
                                        <Skeleton className="h-4 w-20 ml-auto" />
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Skeleton className="h-4 w-20 ml-auto" />
                                    </TableCell>
                                </>
                            )}

                            {/* profile skeletom */}
                            {!dashboardMode && showAlerts && (
                                <>
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Skeleton className="h-7 w-20" />
                                            <Skeleton className="h-7 w-20" />
                                        </div>
                                    </TableCell>
                                    <TableCell className="text-center">
                                        <div className="flex gap-2 justify-center">
                                            <Skeleton className="h-7 w-20" />
                                            <Skeleton className="h-7 w-20" />
                                        </div>
                                    </TableCell>
                                </>
                            )}

                            {showAddedDate && (
                                <TableCell className="text-right">
                                    <Skeleton className="h-4 w-16 ml-auto" />
                                </TableCell>
                            )}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    )
}