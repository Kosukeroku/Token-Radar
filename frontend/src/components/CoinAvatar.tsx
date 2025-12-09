import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { cn } from "@/lib/utils"

interface CoinAvatarProps {
    imageUrl?: string
    symbol: string
    name?: string
    size?: "sm" | "md" | "lg" | "xl"
    className?: string
}

const SIZE_CLASSES = {
    sm: "h-8 w-8",
    md: "h-10 w-10",
    lg: "h-12 w-12",
    xl: "h-16 w-16",
}

const generateColorFromString = (str: string): string => {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }

    const colors = [
        '#3B82F6', // blue
        '#10B981', // emerald
        '#F59E0B', // amber
        '#EF4444', // red
        '#8B5CF6', // violet
        '#EC4899', // pink
        '#14B8A6', // teal
        '#F97316', // orange
        '#6366F1', // indigo
        '#06B6D4', // cyan
    ]

    const colorIndex = Math.abs(hash) % colors.length
    return colors[colorIndex]
}

export function CoinAvatar({
                               imageUrl,
                               symbol,
                               name,
                               size = "md",
                               className,
                           }: CoinAvatarProps) {
    const initials = symbol.length >= 3
        ? symbol.substring(0, 3).toUpperCase()
        : symbol.toUpperCase()

    return (
        <Avatar className={cn(SIZE_CLASSES[size], className)}>
            {imageUrl ? (
                <AvatarImage
                    src={imageUrl}
                    alt={name || symbol}
                    className="object-cover"
                />
            ) : null}
            <AvatarFallback
                className="font-semibold text-white"
                style={{
                    backgroundColor: generateColorFromString(symbol),
                }}
            >
                {initials}
            </AvatarFallback>
        </Avatar>
    )
}