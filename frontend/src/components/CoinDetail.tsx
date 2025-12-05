import React, {useEffect, useMemo} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {useCoinDetail} from '../hooks/useCoinDetail';
import {Loader} from './Loader';
import {ErrorDisplay} from './ErrorDisplay';
import {
    formatDate,
    formatDateTime,
    formatMarketCap,
    formatNumber,
    formatPercentage,
    formatPrice,
    timeAgo
} from '../utils/formatters';
import {SparklineChart} from './SparklineChart';

const CoinDetail: React.FC = () => {
    const {coinId} = useParams<{ coinId: string }>();
    const navigate = useNavigate();
    const {data: coin, isLoading, error, refetch} = useCoinDetail(coinId || '');

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [coinId]);

    const handleTrackClick = () => {
        alert('Track functionality coming soon!');
    };

    // parsing sparkline
    const sparklineData = useMemo(() => {
        if (!coin?.sparklineData) return [];
        try {
            const parsed = JSON.parse(coin.sparklineData);
            return Array.isArray(parsed)
                ? parsed.filter((n): n is number => typeof n === 'number')
                : [];
        } catch {
            return [];
        }
    }, [coin?.sparklineData]);

    if (isLoading) return <Loader/>;

    if (error || !coin) {
        return (
            <ErrorDisplay
                message={`Failed to load data for ${coinId}`}
                onRetry={() => refetch()}
            />
        );
    }

    const isPositive24h = coin.priceChangePercentage24h >= 0;

    return (
        <div className="min-h-screen bg-gray-900 text-white p-4 md:p-6">
            {/* back button*/}
            <button
                onClick={() => navigate(-1)}
                aria-label="Go back to dashboard"
                className="mb-6 px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded-lg flex items-center gap-2 transition-colors"
            >
                ← Back to Dashboard
            </button>

            {/* header with general info */}
            <div className="bg-gray-800 rounded-xl p-6 mb-6">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div className="flex items-center gap-4">
                        <img
                            src={coin.imageUrl}
                            alt={`${coin.name} logo`}
                            className="w-16 h-16 rounded-full"
                            onError={(e) => {
                                (e.target as HTMLImageElement).src = 'https://via.placeholder.com/64';
                            }}
                        />
                        <div>
                            <div className="flex items-center gap-3">
                                <h1 className="text-3xl font-bold">{coin.name}</h1>
                                <button
                                    onClick={handleTrackClick}
                                    aria-label={`Track ${coin.name}`}
                                    className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg text-sm font-semibold transition-colors"
                                >
                                    Track
                                </button>
                            </div>
                            <div className="flex items-center gap-2 mt-1">
                                <span className="text-gray-400 text-lg">{coin.symbol.toUpperCase()}</span>
                                <span className="bg-gray-700 px-2 py-1 rounded text-sm">
                  Rank #{coin.marketCapRank}
                </span>
                            </div>
                        </div>
                    </div>

                    <div className="text-right">
                        <div className="text-3xl font-bold">{formatPrice(coin.currentPrice)}</div>
                        <div
                            className={`text-xl font-semibold ${isPositive24h ? 'text-green-400' : 'text-red-400'}`}
                            aria-label={`24 hour change: ${formatPercentage(coin.priceChangePercentage24h)}`}
                        >
                            {formatPercentage(coin.priceChangePercentage24h)}
                        </div>
                        <div className="text-gray-400 text-sm mt-1">
                            Last updated: {coin.lastUpdated ? formatDateTime(coin.lastUpdated) : 'Recently'}
                        </div>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* left column: graph & market data */}
                <div className="lg:col-span-2 space-y-6">
                    {/* graph with axes */}
                    <div className="bg-gray-800 rounded-xl p-6">
                        <h2 className="text-xl font-bold mb-4">7-Day Price Chart</h2>
                        <div className="h-72">
                            {sparklineData.length > 0 ? (
                                <SparklineChart
                                    data={sparklineData}
                                    isPositive={isPositive24h}
                                    showAxes={true}
                                />
                            ) : (
                                <div className="flex items-center justify-center h-full text-gray-400">
                                    No chart data available
                                </div>
                            )}
                        </div>
                    </div>

                    {/* market data */}
                    <div className="bg-gray-800 rounded-xl p-6">
                        <h2 className="text-xl font-bold mb-6">Market Data</h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-4">
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-400">Market Cap</span>
                                    <span className="font-semibold">{formatMarketCap(coin.marketCap)}</span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-400">24h Volume</span>
                                    <span className="font-semibold">{formatMarketCap(coin.totalVolume)}</span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-400">Circulating Supply</span>
                                    <span className="font-semibold">{formatNumber(coin.circulatingSupply)}</span>
                                </div>
                            </div>

                            <div className="space-y-4">
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-400">24h High</span>
                                    <span className="font-semibold">{formatPrice(coin.high24h)}</span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-400">24h Low</span>
                                    <span className="font-semibold">{formatPrice(coin.low24h)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* right column: statistics */}
                <div className="space-y-6">
                    {/* price changes */}
                    <div className="bg-gray-800 rounded-xl p-6">
                        <h2 className="text-xl font-bold mb-4">Price Changes</h2>
                        <div className="space-y-3">
                            <div className="flex justify-between items-center">
                                <span className="text-gray-400">1h</span>
                                <span
                                    className={`font-semibold ${(coin.priceChangePercentage1h || 0) >= 0 ? 'text-green-400' : 'text-red-400'}`}
                                    aria-label={`1 hour change: ${formatPercentage(coin.priceChangePercentage1h)}`}
                                >
                  {formatPercentage(coin.priceChangePercentage1h)}
                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-gray-400">24h</span>
                                <span
                                    className={`font-semibold ${isPositive24h ? 'text-green-400' : 'text-red-400'}`}
                                    aria-label={`24 hour change: ${formatPercentage(coin.priceChangePercentage24h)}`}
                                >
                  {formatPercentage(coin.priceChangePercentage24h)}
                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-gray-400">7d</span>
                                <span
                                    className={`font-semibold ${(coin.priceChangePercentage7d || 0) >= 0 ? 'text-green-400' : 'text-red-400'}`}
                                    aria-label={`7 day change: ${formatPercentage(coin.priceChangePercentage7d)}`}
                                >
                  {formatPercentage(coin.priceChangePercentage7d)}
                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-gray-400">30d</span>
                                <span
                                    className={`font-semibold ${(coin.priceChangePercentage30d || 0) >= 0 ? 'text-green-400' : 'text-red-400'}`}
                                    aria-label={`30 day change: ${formatPercentage(coin.priceChangePercentage30d)}`}
                                >
                  {formatPercentage(coin.priceChangePercentage30d)}
                </span>
                            </div>
                        </div>
                    </div>

                    {/* ATH with "...ago" */}
                    <div className="bg-gray-800 rounded-xl p-6">
                        <h2 className="text-xl font-bold mb-4">All-Time High</h2>
                        <div className="space-y-4">
                            <div>
                                <div className="flex justify-between items-center mb-1">
                                    <span className="text-gray-400">Price</span>
                                    <span className="font-semibold">{formatPrice(coin.ath)}</span>
                                </div>
                                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-400">
                    {formatDate(coin.athDate)}
                      {coin.athDate && (
                          <span className="ml-1 text-gray-500">
                        ({timeAgo(coin.athDate)} ago)
                      </span>
                      )}
                  </span>
                                    <span
                                        className={`font-semibold ${coin.athChangePercentage >= 0 ? 'text-green-400' : 'text-red-400'}`}
                                        aria-label={`All time high change: ${formatPercentage(coin.athChangePercentage)}`}
                                    >
                    {formatPercentage(coin.athChangePercentage)}
                  </span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="bg-gray-800 rounded-xl p-6">
                        <h2 className="text-xl font-bold mb-4">Market Analytics</h2>
                        <div className="space-y-4">
                            {/* volume/market cap ratio */}
                            <div className="p-3 bg-gray-900/50 rounded-lg">
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-400">Volume/MCap Ratio</span>
                                    <span className="font-bold text-lg text-blue-300">
          {((coin.totalVolume / coin.marketCap) * 100).toFixed(2)}%
        </span>
                                </div>
                                <div className="mt-1 text-sm text-gray-500">
                                    {(() => {
                                        const ratio = (coin.totalVolume / coin.marketCap) * 100;
                                        if (ratio > 10) return 'High trading volume relative to market cap';
                                        if (ratio > 5) return 'Healthy trading activity';
                                        return 'Moderate trading volume';
                                    })()}
                                </div>
                            </div>

                            {/* current vs ATH */}
                            <div className="p-3 bg-gray-900/50 rounded-lg">
                                <div className="flex justify-between items-center mb-2">
                                    <span className="text-gray-400">Current vs ATH</span>
                                    <span className={`font-bold text-lg ${
                                        (coin.currentPrice / coin.ath) > 0.9 ? 'text-green-400' :
                                            (coin.currentPrice / coin.ath) > 0.7 ? 'text-yellow-400' : 'text-red-400'
                                    }`}>
          {((coin.currentPrice / coin.ath) * 100).toFixed(1)}%
        </span>
                                </div>
                                <div className="w-full bg-gray-700 h-2 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-gradient-to-r from-red-500 via-yellow-500 to-green-500"
                                        style={{
                                            width: `${Math.min((coin.currentPrice / coin.ath) * 100, 100)}%`
                                        }}
                                    />
                                </div>
                                <div className="flex justify-between text-xs text-gray-400 mt-1">
                                    <span>0%</span>
                                    <span>ATH: {formatPrice(coin.ath)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CoinDetail;