import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/hooks/useAuth"
import { api } from "@/services/api"
import { cn } from "@/lib/utils"
import type { TrackButtonProps } from "@/types/coin"

export function TrackButton({
                                coinId,
                                coinName = "",
                                size = "default",
                                variant = "outline",
                                className,
                                showText = true,
                                tableStyle = false,
                                onTrackChange,
                            }: TrackButtonProps) {
    const navigate = useNavigate()
    const { isAuthenticated } = useAuth()
    const [isTracked, setIsTracked] = useState(false)
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (isAuthenticated) {
            checkTrackStatus()
        }
    }, [coinId, isAuthenticated])

    const checkTrackStatus = async () => {
        if (!isAuthenticated) return

        try {
            const response = await api.get('/tracked-currencies')
            const trackedCurrencies: Array<{ coinId: string }> = response.data
            const isTracked = trackedCurrencies.some((tc) => tc.coinId === coinId)
            setIsTracked(isTracked)
        } catch (err) {
            console.error('Failed to check track status:', err)
        }
    }

    const handleClick = async (e: React.MouseEvent) => {
        e.stopPropagation()
        setLoading(true)

        if (!isAuthenticated) {
            navigate('/login')
            return
        }

        try {
            if (isTracked) {
                await api.delete(`/tracked-currencies/${coinId}`)
                setIsTracked(false)
                onTrackChange?.(false)
            } else {
                await api.post('/tracked-currencies', { coinId })
                setIsTracked(true)
                onTrackChange?.(true)
            }
        } catch (err: any) {
            console.error('Failed to toggle track:', err)
        } finally {
            setLoading(false)
        }
    }

    const getButtonStyles = () => {
        if (tableStyle) {
            return "h-7 px-3 text-xs font-medium min-w-[65px]"
        }

        switch (size) {
            case "sm":
                return "h-8 px-3 text-sm"
            case "lg":
                return "h-10 px-6 text-base"
            case "icon":
                return "h-9 w-9 p-0"
            default:
                return "h-9 px-4 text-sm"
        }
    }

    const buttonText = tableStyle
        ? (isTracked ? "Untrack" : "Track")
        : (showText ? (isTracked ? "Untrack" : "Track") : "")

    const buttonContent = loading ? (
        <Loader2 className="h-3 w-3 animate-spin" />
    ) : (
        buttonText
    )

    return (
        <Button
            onClick={handleClick}
            disabled={loading}
            variant={variant}
            size={tableStyle ? "sm" : size}
            className={cn(
                getButtonStyles(),
                "font-medium transition-all duration-200",
                isTracked
                    ? "bg-red-500 text-white hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-700"
                    : "bg-green-500 text-white hover:bg-green-600 dark:bg-green-600 dark:hover:bg-green-700",
                tableStyle && "hover:translate-y-[-1px] hover:shadow-sm",
                className
            )}
            aria-label={isTracked ? `Untrack ${coinName}` : `Track ${coinName}`}
        >
            {buttonContent}
        </Button>
    )
}