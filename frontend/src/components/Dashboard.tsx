import { useSearchParams } from 'react-router-dom'
import { useCoins } from '../hooks/useCoins'
import { useSearchCoins } from '../hooks/useSearchCoins'
import { useDebounce } from '../hooks/useDebounce'
import { formatPrice, formatMarketCap } from '../utils/formatters'
import { Loader } from '../components/Loader'
import { ErrorDisplay } from '../components/ErrorDisplay'
import { useState, useRef, useEffect } from 'react'

const SearchInput: React.FC<{
    value: string;
    onChange: (value: string) => void;
    loading?: boolean;
    inputRef: React.RefObject<HTMLInputElement | null>;
}> = ({ value, onChange, loading, inputRef }) => (
    <div className="relative max-w-md">
        <input
            ref={inputRef}
            type="text"
            placeholder="Search coins..."
            value={value}
            onChange={(e) => onChange(e.target.value)}
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 pl-10 text-white placeholder-gray-400 focus:outline-none focus:border-blue-500"
        />
        <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
            🔍
        </div>
        {loading && (
            <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500"></div>
            </div>
        )}
    </div>
)

const Dashboard: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams()
    const [searchTerm, setSearchTerm] = useState('')
    const searchInputRef = useRef<HTMLInputElement>(null)

    const debouncedSearchTerm = useDebounce(searchTerm, 300)
    const currentPage = Math.max(0, parseInt(searchParams.get('page') || '1') - 1)

    // main table and search
    const { data: coinsData, isLoading: coinsLoading, error: coinsError } = useCoins(currentPage)
    const { data: searchData, isLoading: searchLoading, error: searchError } = useSearchCoins(debouncedSearchTerm)

    // which data to show
    const isSearchMode = !!debouncedSearchTerm.trim()
    const coins = isSearchMode ? searchData : coinsData?.coins
    const isLoading = isSearchMode ? searchLoading : coinsLoading
    const error = isSearchMode ? searchError : coinsError

    useEffect(() => {
        if (searchInputRef.current) {
            searchInputRef.current.focus()
        }
    }, [])

    useEffect(() => {
        if (searchInputRef.current && !searchLoading && searchTerm) {
            searchInputRef.current.focus()
        }
    }, [searchLoading, searchTerm])

    const handlePageChange = (newPage: number) => {
        const newSearchParams = new URLSearchParams(searchParams)
        if (newPage === 0) {
            newSearchParams.delete('page')
        } else {
            newSearchParams.set('page', (newPage + 1).toString())
        }
        setSearchParams(newSearchParams)
    }

    if (isLoading) return <Loader />

    if (error) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-4">
                <div className="mb-6">
                    <h1 className="text-3xl font-bold mb-2 font-sans">
                        {isSearchMode ? 'Search Results' : 'Live Cryptocurrency Prices'}
                    </h1>
                    <SearchInput
                        value={searchTerm}
                        onChange={setSearchTerm}
                        loading={searchLoading}
                        inputRef={searchInputRef}
                    />
                </div>
                <ErrorDisplay
                    message={`Failed to load data: ${error.message}`}
                    onRetry={() => window.location.reload()}
                />
            </div>
        )
    }

    if (!coins?.length) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-4">
                {/* keeping header and search */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold mb-2 font-sans">
                        {isSearchMode ? 'Search Results' : 'Live Cryptocurrency Prices'}
                    </h1>
                    <SearchInput
                        value={searchTerm}
                        onChange={setSearchTerm}
                        loading={searchLoading}
                        inputRef={searchInputRef}
                    />
                </div>

                {/* empty table with 'no coins found' message */}
                <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full min-w-[700px]">
                            <thead>
                            <tr className="border-b border-gray-700">
                                <th className="text-left py-3 px-2 text-gray-400 font-semibold text-sm w-12">#</th>
                                <th className="text-left py-3 px-2 text-gray-400 font-semibold text-sm">Coin</th>
                                <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Price</th>
                                <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">24h Change</th>
                                <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Market Cap</th>
                                <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Volume (24h)</th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                    <div className="flex justify-center items-center py-16 text-gray-400">
                        {isSearchMode ? "No coins found for your search" : "No cryptocurrencies found"}
                    </div>
                </div>
            </div>
        )
    }

    return (
        <div className="min-h-screen bg-gray-900 text-white p-4">
            {/* header and search */}
            <div className="mb-6">
                <h1 className="text-3xl font-bold mb-2 font-sans">
                    {isSearchMode ? 'Search Results' : 'Live Cryptocurrency Prices'}
                </h1>
                <SearchInput
                    value={searchTerm}
                    onChange={setSearchTerm}
                    loading={searchLoading}
                    inputRef={searchInputRef}
                />
            </div>

            {/* table */}
            <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full min-w-[700px]">
                        <thead>
                        <tr className="border-b border-gray-700">
                            <th className="text-left py-3 px-2 text-gray-400 font-semibold text-sm w-12">#</th>
                            <th className="text-left py-3 px-2 text-gray-400 font-semibold text-sm">Coin</th>
                            <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Price</th>
                            <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">24h Change</th>
                            <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Market Cap</th>
                            <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Volume (24h)</th>
                        </tr>
                        </thead>
                        <tbody>
                        {coins.map((coin) => (
                            <tr key={coin.id} className="border-b border-gray-700 hover:bg-gray-700/50">
                                <td className="py-3 px-2 text-gray-400 text-sm w-12">{coin.marketCapRank}</td>
                                <td className="py-3 px-2">
                                    <div className="flex items-center space-x-2">
                                        <img
                                            src={coin.imageUrl}
                                            alt={coin.name}
                                            className="w-8 h-8"
                                            onError={(e) => {
                                                (e.target as HTMLImageElement).src = 'https://via.placeholder.com/32';
                                            }}
                                        />
                                        <div>
                                            <div className="font-medium text-white text-sm">{coin.name}</div>
                                            <div className="text-gray-400 text-xs">{coin.symbol.toUpperCase()}</div>
                                        </div>
                                    </div>
                                </td>
                                <td className="py-3 px-4 text-right text-white text-sm">
                                    {formatPrice(coin.currentPrice)}
                                </td>
                                <td className="py-3 px-4 text-right">
                    <span className={`text-sm ${
                        coin.priceChange24h >= 0 ? 'text-green-400' : 'text-red-400'
                    }`}>
                      {coin.priceChangePercentage24h?.toFixed(2)}%
                    </span>
                                </td>
                                <td className="py-3 px-4 text-right text-white text-sm">
                                    {formatMarketCap(coin.marketCap)}
                                </td>
                                <td className="py-3 px-4 text-right text-white text-sm">
                                    {formatMarketCap(coin.totalVolume)}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* pagination (showing only if not in the search mode) */}
            {!isSearchMode && coinsData && coinsData.totalPages > 1 && (
                <div className="flex justify-center mt-6 space-x-2">
                    <button
                        onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                        disabled={currentPage === 0}
                        className="px-3 py-1 bg-gray-800 border border-gray-700 rounded text-white text-sm disabled:opacity-50 hover:bg-gray-700"
                    >
                        Previous
                    </button>

                    <span className="px-3 py-1 text-sm">
            Page {currentPage + 1} of {coinsData.totalPages}
          </span>

                    <button
                        onClick={() => handlePageChange(Math.min(coinsData.totalPages - 1, currentPage + 1))}
                        disabled={currentPage >= coinsData.totalPages - 1}
                        className="px-3 py-1 bg-gray-800 border border-gray-700 rounded text-white text-sm disabled:opacity-50 hover:bg-gray-700"
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    )
}

export default Dashboard