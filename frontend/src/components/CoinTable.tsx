import React from 'react';
import { formatPrice, formatPercentage, formatMarketCap } from '../utils/formatters';
import TrackButton from './TrackButton';
import CoinAvatar from './CoinAvatar';

interface Coin {
    id: string;
    name: string;
    symbol: string;
    imageUrl: string;
    currentPrice: number;
    priceChangePercentage24h: number;
    marketCapRank: number;
    marketCap: number;
    totalVolume: number;
}

interface CoinTableProps {
    coins: Coin[];
    onRowClick?: (coinId: string) => void;
    showActions?: boolean;
    onUntrack?: (coinId: string) => void;
    showAddedDate?: boolean;
    addedDates?: { [coinId: string]: string };
    showTrackButton?: boolean;
}

const CoinTable: React.FC<CoinTableProps> = ({
                                                 coins,
                                                 onRowClick,
                                                 showActions = false,
                                                 onUntrack,
                                                 showAddedDate = false,
                                                 addedDates = {},
                                                 showTrackButton = false,
                                             }) => {
    return (
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
                        {showAddedDate && (
                            <th className="text-right py-3 px-4 text-gray-400 font-semibold text-sm">Added</th>
                        )}
                    </tr>
                    </thead>
                    <tbody>
                    {coins.map((coin) => (
                        <tr
                            key={coin.id}
                            className="border-b border-gray-700 hover:bg-gray-700/50 cursor-pointer transition-colors duration-150"
                            onClick={() => onRowClick && onRowClick(coin.id)}
                        >
                            <td className="py-3 px-2 text-gray-400 text-sm w-12">
                                {coin.marketCapRank}
                            </td>
                            <td className="py-3 px-2">
                                <div className="flex items-center gap-2">
                                    {/* buttons left from the icon */}
                                    <div className="flex items-center gap-1">
                                        {/* Track/Untrack button */}
                                        {showTrackButton && (
                                            <div onClick={(e) => e.stopPropagation()}>
                                                <TrackButton
                                                    coinId={coin.id}
                                                    coinName={coin.name}
                                                />
                                            </div>
                                        )}

                                        {/* Untrack button in profile */}
                                        {showActions && onUntrack && (
                                            <div onClick={(e) => e.stopPropagation()}>
                                                <TrackButton
                                                    coinId={coin.id}
                                                    coinName={coin.name}
                                                />
                                            </div>
                                        )}
                                    </div>

                                    {/* avatar with initials */}
                                    <CoinAvatar
                                        imageUrl={coin.imageUrl}
                                        symbol={coin.symbol}
                                        size={32}
                                    />

                                    {/* name and symbol */}
                                    <div className="min-w-0 flex-1">
                                        <div className="font-medium text-white text-sm truncate">{coin.name}</div>
                                        <div className="text-gray-400 text-xs">{coin.symbol.toUpperCase()}</div>
                                    </div>
                                </div>
                            </td>
                            <td className="py-3 px-4 text-right text-white text-sm">
                                {formatPrice(coin.currentPrice)}
                            </td>
                            <td className="py-3 px-4 text-right">
                  <span className={`text-sm ${
                      coin.priceChangePercentage24h >= 0 ? 'text-green-400' : 'text-red-400'
                  }`}>
                    {formatPercentage(coin.priceChangePercentage24h)}
                  </span>
                            </td>
                            <td className="py-3 px-4 text-right text-white text-sm">
                                {coin.marketCap ? formatMarketCap(coin.marketCap) : 'N/A'}
                            </td>
                            <td className="py-3 px-4 text-right text-white text-sm">
                                {coin.totalVolume ? formatMarketCap(coin.totalVolume) : 'N/A'}
                            </td>
                            {showAddedDate && (
                                <td className="py-3 px-4 text-right text-gray-400 text-sm">
                                    {addedDates[coin.id] || 'N/A'}
                                </td>
                            )}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {coins.length === 0 && (
                <div className="flex justify-center items-center py-16 text-gray-400">
                    No cryptocurrencies found
                </div>
            )}
        </div>
    );
};

export default CoinTable;