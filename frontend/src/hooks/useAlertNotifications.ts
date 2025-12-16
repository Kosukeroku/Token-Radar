
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '@/services/api'

interface Notification {
    id: number
    coinId: string
    coinName: string
    coinSymbol: string
    message: string
    isRead: boolean
    createdAt: string
    triggeredPrice?: number
}

export const useAlertNotifications = () => {
    const queryClient = useQueryClient()

    const { data: notifications = [], isLoading, refetch } = useQuery({
        queryKey: ['alert-notifications'],
        queryFn: async (): Promise<Notification[]> => {
            const response = await api.get('/alerts/notifications')
            return response.data.map((item: any) => ({
                id: item.id,
                coinId: item.coinId,
                coinName: item.coinName,
                coinSymbol: item.coinSymbol,
                message: item.notificationMessage || "Price alert triggered",
                isRead: item.isRead || false,
                createdAt: item.triggeredAt || item.createdAt,
                triggeredPrice: item.triggeredPrice
            }))
        },
        staleTime: 30000, // 30 sec
    })

    // getting an unread amount
    const { data: stats } = useQuery({
        queryKey: ['alert-stats'],
        queryFn: async () => {
            const response = await api.get('/alerts/stats')
            return response.data
        },
    })

    // marking all as read
    const markAllAsReadMutation = useMutation({
        mutationFn: async () => {
            await api.post('/alerts/read-all')
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['alert-notifications'] })
            queryClient.invalidateQueries({ queryKey: ['alert-stats'] })
        },
    })

    // deleting one notification
    const deleteNotificationMutation = useMutation({
        mutationFn: async (alertId: number) => {
            await api.delete(`/alerts/${alertId}`)
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['alert-notifications'] })
            queryClient.invalidateQueries({ queryKey: ['alert-stats'] })
        },
    })

    // clearing all
    const clearAllNotificationsMutation = useMutation({
        mutationFn: async () => {
            await api.post('/alerts/clear-read')
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['alert-notifications'] })
            queryClient.invalidateQueries({ queryKey: ['alert-stats'] })
        },
    })

    return {
        notifications,
        unreadCount: stats?.unreadCount || 0,
        isLoading,
        markAllAsRead: () => markAllAsReadMutation.mutate(),
        deleteNotification: (alertId: number) => deleteNotificationMutation.mutate(alertId),
        clearAllNotifications: () => clearAllNotificationsMutation.mutate(),
        refetch,
    }
}