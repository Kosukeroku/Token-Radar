import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { api } from '../services/api';
import { formatDate } from '../utils/formatters';
import { Loader } from './Loader';
import { ErrorDisplay } from './ErrorDisplay';
import CoinTable from "./CoinTable";

// Определяем типы
interface TrackedCurrencyDto {
    id: number;
    coinId: string;
    coinName: string;
    coinSymbol: string;
    coinImageUrl: string;
    addedAt: string;
    currentPrice: number;
    priceChangePercentage24h: number;
    marketCapRank: number;
    marketCap: number;
    totalVolume: number;
}

interface UserProfileDto {
    id: number;
    username: string;
    email: string;
    createdAt: string;
    trackedCurrencies: TrackedCurrencyDto[];
    trackedCount: number;
}

interface CoinTableData {
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

const Profile: React.FC = () => {
    const navigate = useNavigate();
    const { isAuthenticated, loading: authLoading } = useAuth();

    const [profile, setProfile] = useState<UserProfileDto | null>(null);
    const [trackedCoins, setTrackedCoins] = useState<TrackedCurrencyDto[]>([]);
    const [trackedCount, setTrackedCount] = useState<number>(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!isAuthenticated && !authLoading) {
            navigate('/login');
            return;
        }

        if (isAuthenticated) {
            fetchProfileData();
        }
    }, [isAuthenticated, authLoading, navigate]);

    const fetchProfileData = async () => {
        try {
            setLoading(true);
            const response = await api.get<UserProfileDto>('/profile');
            setProfile(response.data);
            setTrackedCoins(response.data.trackedCurrencies || []);
            setTrackedCount(response.data.trackedCount || response.data.trackedCurrencies.length);
        } catch (err: any) {
            setError('Failed to load profile data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleUntrack = async (coinId: string) => {
        try {
            await api.delete(`/tracked-currencies/${coinId}`);

            // refreshing the coins list
            const updatedCoins = trackedCoins.filter(coin => coin.coinId !== coinId);
            setTrackedCoins(updatedCoins);
            setTrackedCount(updatedCoins.length);

            // refreshing the profile
            setProfile((prev) => prev ? {
                ...prev,
                trackedCurrencies: prev.trackedCurrencies.filter((coin) => coin.coinId !== coinId),
                trackedCount: updatedCoins.length
            } : null);
        } catch (err) {
            console.error('Failed to untrack currency:', err);
        }
    };

    const handleRowClick = (coinId: string) => {
        navigate(`/coin/${coinId}`);
    };

    // transforms TrackedCurrency into CoinTable format
    const mapToCoinTableData = (): CoinTableData[] => {
        if (!trackedCoins || trackedCoins.length === 0) return [];

        return trackedCoins.map(tc => ({
            id: tc.coinId,
            name: tc.coinName,
            symbol: tc.coinSymbol,
            imageUrl: tc.coinImageUrl,
            currentPrice: tc.currentPrice,
            priceChangePercentage24h: tc.priceChangePercentage24h,
            marketCapRank: tc.marketCapRank,
            marketCap: tc.marketCap,
            totalVolume: tc.totalVolume
        }));
    };

    // gets added dates
    const getAddedDates = (): Record<string, string> => {
        if (!trackedCoins || trackedCoins.length === 0) return {};

        const dates: Record<string, string> = {};
        trackedCoins.forEach(tc => {
            dates[tc.coinId] = formatDate(tc.addedAt);
        });
        return dates;
    };

    if (authLoading) return <Loader />;

    if (!isAuthenticated) {
        return null;
    }

    if (loading) return <Loader />;

    if (error) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <ErrorDisplay
                    message={error}
                    onRetry={fetchProfileData}
                />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-900 text-white p-4 md:p-6">
            {/* Back to Dashboard button */}
            <button
                onClick={() => navigate('/')}
                className="mb-6 px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded-lg flex items-center gap-2 transition-colors"
            >
                ← Back to Dashboard
            </button>

            {/* info header */}
            <div className="mb-8">
                <h1 className="text-3xl font-bold mb-2">{profile?.username}</h1>
                <div className="text-gray-400">
                    <p className="text-sm">
                        Joined {profile?.createdAt ? formatDate(profile.createdAt) : 'recently'}
                    </p>
                    <p className="text-sm mt-1">
                        Tracking {trackedCount} cryptocurrencies
                    </p>
                </div>
            </div>

            {/* table */}
            <CoinTable
                coins={mapToCoinTableData()}
                onRowClick={handleRowClick}
                showActions={true}
                onUntrack={handleUntrack} //
                showAddedDate={true}
                addedDates={getAddedDates()}
            />

            {/* message for 0 tracked coins */}
            {trackedCoins.length === 0 && (
                <div className="text-center mt-8 text-gray-400">
                    <p className="mb-4">You are not tracking any cryptocurrencies yet.</p>
                    <a
                        href="/"
                        className="text-blue-400 hover:text-blue-300 font-medium"
                    >
                        Browse cryptocurrencies to start tracking →
                    </a>
                </div>
            )}
        </div>
    );
};

export default Profile;