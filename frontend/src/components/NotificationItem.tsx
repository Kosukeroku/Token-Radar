import { X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { formatDateTime } from "@/utils/formatters"
import { useAlertNotifications } from "@/hooks/useAlertNotifications"
import { cn } from "@/lib/utils"

interface NotificationItemProps {
    notification: {
        id: number
        coinName: string
        coinSymbol: string
        message: string
        isRead: boolean
        createdAt: string
        triggeredPrice?: number
    }
}

export function NotificationItem({ notification }: NotificationItemProps) {
    const { deleteNotification } = useAlertNotifications()

    return (
        <div className={cn(
            "p-4 hover:bg-accent/50 transition-colors group",
            !notification.isRead && "bg-blue-500/10"
        )}>
            <div className="flex justify-between items-start mb-2">
                <div className="flex items-center gap-2">
          <span className="font-medium text-sm">
            {notification.coinName}
          </span>
                    <span className="text-xs text-muted-foreground">
            {notification.coinSymbol.toUpperCase()}
          </span>
                    {!notification.isRead && (
                        <span className="h-2 w-2 rounded-full bg-blue-500 animate-pulse" />
                    )}
                </div>

                <div className="flex items-center gap-2">
          <span className="text-xs text-muted-foreground">
            {formatDateTime(notification.createdAt)}
          </span>
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={(e) => {
                            e.stopPropagation()
                            deleteNotification(notification.id)
                        }}
                        className="h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity hover:bg-destructive/10 hover:text-destructive"
                    >
                        <X className="h-3 w-3" />
                    </Button>
                </div>
            </div>

            <p className="text-sm mb-1">
                {notification.message}
            </p>

            {notification.triggeredPrice && (
                <div className="text-xs text-muted-foreground">
                    Price: ${notification.triggeredPrice.toFixed(2)}
                </div>
            )}
        </div>
    )
}