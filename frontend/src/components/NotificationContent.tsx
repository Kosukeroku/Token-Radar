import { Bell } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import { NotificationItem } from "./NotificationItem"

interface NotificationContentProps {
    isLoading: boolean
    notifications: Array<{
        id: number
        coinName: string
        coinSymbol: string
        message: string
        isRead: boolean
        createdAt: string
        triggeredPrice?: number
    }>
}

export function NotificationContent({ isLoading, notifications }: NotificationContentProps) {
    if (isLoading) {
        return (
            <div className="space-y-3 p-4">
                {[...Array(3)].map((_, i) => (
                    <Skeleton key={i} className="h-16 w-full" />
                ))}
            </div>
        )
    }

    if (notifications.length === 0) {
        return (
            <div className="p-8 text-center">
                <Bell className="h-12 w-12 mx-auto mb-4 text-muted-foreground/50" />
                <p className="text-muted-foreground">No notifications yet</p>
            </div>
        )
    }

    return (
        <div className="max-h-[400px] overflow-y-auto divide-y">
            {notifications.map((notification) => (
                <NotificationItem
                    key={notification.id}
                    notification={notification}
                />
            ))}
        </div>
    )
}