import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { DataTable } from "./DataTable"
import { useAuth } from "@/hooks/useAuth"
import { api } from "@/services/api"
import { formatDate } from "@/utils/formatters"
import { Loader } from "./Loader"
import { ErrorDisplay } from "./ErrorDisplay"
import type { TrackedCurrency, UserProfile, Coin } from "@/types/coin"

export function Profile() {
    const navigate = useNavigate()
    const { isAuthenticated, loading: authLoading } = useAuth()
    const [profile, setProfile] = useState<UserProfile | null>(null)
    const [trackedCoins, setTrackedCoins] = useState<TrackedCurrency[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState("")

    useEffect(() => {
        if (!isAuthenticated && !authLoading) {
            navigate('/login')
            return
        }

        if (isAuthenticated) {
            fetchProfileData()
        }
    }, [isAuthenticated, authLoading, navigate])

    const fetchProfileData = async () => {
        try {
            setLoading(true)
            const response = await api.get<UserProfile>('/profile')
            setProfile(response.data)
            setTrackedCoins(response.data.trackedCurrencies || [])
        } catch (err: any) {
            setError('Failed to load profile data')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    const handleUntrack = async (coinId: string) => {
        try {
            await api.delete(`/tracked-currencies/${coinId}`)
            setTrackedCoins(prev => prev.filter(coin => coin.coinId !== coinId))

            setProfile(prev => prev ? {
                ...prev,
                trackedCurrencies: prev.trackedCurrencies.filter(coin => coin.coinId !== coinId),
                trackedCount: prev.trackedCount - 1
            } : null)
        } catch (err) {
            console.error('Failed to untrack currency:', err)
            fetchProfileData()
        }
    }

    const handleRowClick = (coin: Coin) => {
        navigate(`/coin/${coin.id}`)
    }

    // transforming data for DataTable
    const tableData: Coin[] = trackedCoins.map(tc => ({
        id: tc.coinId,
        name: tc.coinName,
        symbol: tc.coinSymbol,
        imageUrl: tc.coinImageUrl,
        currentPrice: tc.currentPrice,
        priceChangePercentage24h: tc.priceChangePercentage24h,
        marketCapRank: tc.marketCapRank,
        marketCap: tc.marketCap,
        totalVolume: tc.totalVolume
    }))

    const addedDates = trackedCoins.reduce((acc, tc) => {
        acc[tc.coinId] = formatDate(tc.addedAt)
        return acc
    }, {} as Record<string, string>)

    if (authLoading || loading) return <Loader />

    if (!isAuthenticated) {
        return null
    }

    if (error) {
        return (
            <div className="container mx-auto px-4 py-8">
                <ErrorDisplay
                    message={error}
                    onRetry={fetchProfileData}
                />
            </div>
        )
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <Button
                variant="ghost"
                onClick={() => navigate('/')}
                className="mb-6 text-base h-11 px-5"
            >
                <ArrowLeft className="mr-2 h-5 w-5" />
                Back to Dashboard
            </Button>

            <Card className="mb-6">
                <CardHeader>
                    <CardTitle className="text-2xl">{profile?.username}</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="space-y-2 text-muted-foreground">
                        <p>Joined {profile?.createdAt ? formatDate(profile.createdAt) : 'recently'}</p>
                        {/* ИСПОЛЬЗУЕМ trackedCount из профиля или длину массива */}
                        <p>Tracking {profile?.trackedCount || trackedCoins.length} cryptocurrencies</p>
                        <p>Email: {profile?.email}</p>
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Tracked Cryptocurrencies</CardTitle>
                </CardHeader>
                <CardContent>
                    {trackedCoins.length > 0 ? (
                        <DataTable
                            coins={tableData}
                            onRowClick={handleRowClick}
                            showActions={true}
                            onUntrack={handleUntrack}
                            showAddedDate={true}
                            addedDates={addedDates}
                            showTrackButton={false}
                        />
                    ) : (
                        <div className="text-center py-12">
                            <h3 className="text-xl font-semibold mb-2">No tracked coins yet</h3>
                            <p className="text-muted-foreground mb-6">
                                Start tracking cryptocurrencies to see them here
                            </p>
                            <Button onClick={() => navigate('/')}>
                                Browse cryptocurrencies to start tracking
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    )
}