import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

interface PriceChangeBadgeProps {
    value: number | null | undefined
    className?: string
}

export function PriceChangeBadge({
                                     value,
                                     className,
                                 }: PriceChangeBadgeProps) {
    const isInvalid = value === null || value === undefined || isNaN(value)

    if (isInvalid) {
        return (
            <Badge
                variant="outline"
                className={cn(
                    "font-medium bg-gray-50 border-gray-200 text-gray-600",
                    "dark:bg-gray-950/20 dark:border-gray-800 dark:text-gray-400",
                    className
                )}
            >
                N/A
            </Badge>
        )
    }

    const isPositive = value > 0
    const isNeutral = value === 0

    const formattedValue = `${isPositive ? '+' : ''}${value.toFixed(2)}%`

    return (
        <Badge
            variant="outline"
            className={cn(
                "font-medium",
                isPositive
                    ? "text-green-600 bg-green-50 border-green-200 dark:text-green-400 dark:bg-green-950/20 dark:border-green-800"
                    : isNeutral
                        ? "text-gray-600 bg-gray-50 border-gray-200 dark:text-gray-400 dark:bg-gray-950/20 dark:border-gray-800"
                        : "text-red-600 bg-red-50 border-red-200 dark:text-red-400 dark:bg-red-950/20 dark:border-red-800",
                className
            )}
        >
            {formattedValue}
        </Badge>
    )
}