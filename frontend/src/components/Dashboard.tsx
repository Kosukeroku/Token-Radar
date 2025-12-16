import { useState, useRef } from "react"
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Search } from "lucide-react"
import { DataTable } from "./DataTable"
import { useCoins } from "@/hooks/useCoins"
import { useSearchCoins } from "@/hooks/useSearchCoins"
import { useDebounce } from "@/hooks/useDebounce"
import { Loader } from "./Loader"
import { ErrorDisplay } from "./ErrorDisplay"

export function Dashboard() {
    const navigate = useNavigate()
    const [searchParams, setSearchParams] = useSearchParams()
    const [searchTerm, setSearchTerm] = useState('')
    const searchInputRef = useRef<HTMLInputElement>(null)

    const debouncedSearchTerm = useDebounce(searchTerm, 300)
    const currentPage = Math.max(0, parseInt(searchParams.get('page') || '1') - 1)

    const { data: coinsData, isLoading: coinsLoading, error: coinsError } = useCoins(currentPage)
    const { data: searchData, isLoading: searchLoading, error: searchError } = useSearchCoins(debouncedSearchTerm)

    const isSearchMode = !!debouncedSearchTerm.trim()
    const coins = isSearchMode ? searchData : coinsData?.coins
    const isLoading = isSearchMode ? searchLoading : coinsLoading
    const error = isSearchMode ? searchError : coinsError

    const isEmptySearch = isSearchMode && !isLoading && (!searchData || searchData.length === 0)

    const handleRowClick = (coin: any) => {
        navigate(`/coin/${coin.id}`)
    }

    const handlePageChange = (newPage: number) => {
        const newSearchParams = new URLSearchParams(searchParams)
        if (newPage === 0) {
            newSearchParams.delete('page')
        } else {
            newSearchParams.set('page', (newPage + 1).toString())
        }
        setSearchParams(newSearchParams)
    }

    const handleClearSearch = () => {
        setSearchTerm('')
        if (searchInputRef.current) {
            searchInputRef.current.focus()
        }
    }

    if (isLoading) return <Loader />

    if (error) {
        return (
            <div className="container mx-auto px-4 py-8">
                <Card>
                    <CardHeader>
                        <CardTitle>Live Cryptocurrency Prices</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="relative max-w-md mb-6">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
                            <Input
                                ref={searchInputRef}
                                placeholder="Search coins..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="pl-10"
                            />
                        </div>
                        <ErrorDisplay
                            message={`Failed to load data: ${error.message}`}
                            onRetry={() => window.location.reload()}
                        />
                    </CardContent>
                </Card>
            </div>
        )
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <Card className="mb-6">
                <CardHeader>
                    <CardTitle className="text-3xl font-bold">
                        {isSearchMode ? 'Search Results' : 'Live Cryptocurrency Prices'}
                    </CardTitle>
                </CardHeader>
                <CardContent className="pt-0">
                    <div className="relative max-w-md">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
                        <Input
                            ref={searchInputRef}
                            placeholder="Search coins..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10"
                        />
                    </div>
                </CardContent>
            </Card>

            {isEmptySearch ? (
                <Card>
                    <CardContent className="p-12 text-center">
                        <div className="mb-4">
                            <Search className="h-12 w-12 mx-auto text-muted-foreground" />
                        </div>
                        <h3 className="text-xl font-semibold mb-2">No coins found</h3>
                        <p className="text-muted-foreground mb-6">
                            No cryptocurrencies match "{debouncedSearchTerm}"
                        </p>
                        <Button
                            onClick={handleClearSearch}
                            variant="outline"
                        >
                            Clear search
                        </Button>
                    </CardContent>
                </Card>
            ) : (
                <Card>
                    <CardContent className="p-6">
                        <DataTable
                            coins={coins || []}
                            onRowClick={handleRowClick}
                            showTrackButton={true}
                            isLoading={isLoading}
                            dashboardMode={true}
                        />

                        {/* pagination */}
                        {!isSearchMode && coinsData && coinsData.totalPages > 1 && (
                            <div className="flex items-center justify-between mt-6 pt-6 border-t">
                                <div className="text-sm text-muted-foreground">
                                    Page {currentPage + 1} of {coinsData.totalPages}
                                </div>
                                <div className="flex gap-2">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                                        disabled={currentPage === 0}
                                    >
                                        Previous
                                    </Button>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => handlePageChange(Math.min(coinsData.totalPages - 1, currentPage + 1))}
                                        disabled={currentPage >= coinsData.totalPages - 1}
                                    >
                                        Next
                                    </Button>
                                </div>
                            </div>
                        )}
                    </CardContent>
                </Card>
            )}
        </div>
    )
}