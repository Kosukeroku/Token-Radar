import { useState, useEffect, useRef } from "react"
import { Bell, X } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { NotificationContent } from "./NotificationContent"
import { useAlertNotifications } from "@/hooks/useAlertNotifications"

export function NotificationBell() {
    const [isOpen, setIsOpen] = useState(false)
    const dropdownRef = useRef<HTMLDivElement>(null)
    const buttonRef = useRef<HTMLButtonElement>(null)

    const {
        notifications,
        unreadCount,
        isLoading,
        markAllAsRead,
        clearAllNotifications,
        refetch
    } = useAlertNotifications()

    // closing on outside click
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target as Node) &&
                buttonRef.current &&
                !buttonRef.current.contains(event.target as Node)
            ) {
                setIsOpen(false)
            }
        }

        document.addEventListener("mousedown", handleClickOutside)
        return () => document.removeEventListener("mousedown", handleClickOutside)
    }, [])

    // bell click handler
    const handleBellClick = () => {
        const newIsOpen = !isOpen
        setIsOpen(newIsOpen)

        if (newIsOpen) {
            // on opening
            // loading notifications (if not loaded yet)...
            refetch()
            // ...and marking triggered as read
            if (unreadCount > 0) {
                markAllAsRead()
            }
        }
    }

    return (
        <div className="relative">
            <Button
                ref={buttonRef}
                variant="ghost"
                size="icon"
                className="relative hover:bg-accent"
                onClick={handleBellClick}
            >
                <Bell className="h-5 w-5" />
                {unreadCount > 0 && (
                    <Badge
                        variant="destructive"
                        className="absolute -top-1 -right-1 h-5 min-w-5 px-1 flex items-center justify-center"
                    >
                        {unreadCount > 99 ? "99+" : unreadCount}
                    </Badge>
                )}
            </Button>

            {isOpen && (
                <div
                    ref={dropdownRef}
                    className="absolute right-0 top-full mt-2 z-50 w-96 rounded-lg border bg-card shadow-lg"
                >
                    <div className="flex items-center justify-between border-b px-4 py-3">
                        <h3 className="font-semibold">Notifications</h3>
                        {notifications.length > 0 && (
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={(e) => {
                                    e.stopPropagation()
                                    clearAllNotifications()
                                }}
                                className="h-8 px-2 text-xs text-destructive hover:bg-destructive/10"
                            >
                                <X className="h-3 w-3 mr-1" />
                                Clear all
                            </Button>
                        )}
                    </div>

                    <NotificationContent
                        isLoading={isLoading}
                        notifications={notifications}
                    />
                </div>
            )}
        </div>
    )
}