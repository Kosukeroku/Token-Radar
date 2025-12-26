import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '@/services/api'
import { webSocketService } from '@/services/websocket'
import { useEffect } from 'react'

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

interface AlertStats {
    totalAlerts?: number
    activeAlerts?: number
    triggeredAlerts?: number
    readAlerts?: number
    unreadCount?: number
}

export const useAlertNotifications = () => {
    const queryClient = useQueryClient()

    useEffect(() => {
        const handleWebSocketAlert = (alert: any) => {
            // refreshing notification count
            queryClient.setQueryData(['alert-notifications'], (oldData: Notification[] = []) => {
                const newNotification: Notification = {
                    id: alert.alertId,
                    coinId: alert.coinId,
                    coinName: alert.coinName,
                    coinSymbol: alert.coinSymbol,
                    message: alert.notificationMessage,
                    isRead: false,
                    createdAt: alert.triggeredAt,
                    triggeredPrice: alert.triggeredPrice
                };
                return [newNotification, ...oldData];
            });

            // refreshing statistics
            queryClient.setQueryData(['alert-stats'], (oldStats: AlertStats = {}) => ({
                ...oldStats,
                unreadCount: (oldStats.unreadCount || 0) + 1,
                triggeredAlerts: (oldStats.triggeredAlerts || 0) + 1,
                totalAlerts: (oldStats.totalAlerts || 0) + 1
            }));
        };

        webSocketService.connect(handleWebSocketAlert);
        return () => webSocketService.disconnect();
    }, [queryClient])

    const { data: notifications = [], isLoading, refetch } = useQuery({
        queryKey: ['alert-notifications'],
        queryFn: async (): Promise<Notification[]> => {
            const response = await api.get('/alerts/notifications');
            return response.data.map((item: any) => ({
                id: item.id,
                coinId: item.coinId,
                coinName: item.coinName,
                coinSymbol: item.coinSymbol,
                message: item.notificationMessage || "Price alert triggered",
                isRead: item.isRead || false,
                createdAt: item.triggeredAt || item.createdAt,
                triggeredPrice: item.triggeredPrice
            }));
        },
        staleTime: 30000,
    })

    // gets an unread amount
    const { data: stats } = useQuery({
        queryKey: ['alert-stats'],
        queryFn: async (): Promise<AlertStats> => {
            const response = await api.get('/alerts/stats');
            return response.data;
        },
    })

    // marks all as read
    const markAllAsReadMutation = useMutation({
        mutationFn: async () => {
            await api.post('/alerts/read-all');
        },
        onSuccess: () => {
            queryClient.setQueryData(['alert-notifications'], (oldData: Notification[] = []) => {
                return oldData.map(notification => ({
                    ...notification,
                    isRead: true
                }));
            });
            queryClient.setQueryData(['alert-stats'], (oldStats: AlertStats = {}) => ({
                ...oldStats,
                unreadCount: 0
            }));
        },
    })

    // deletes one notification
    const deleteNotificationMutation = useMutation({
        mutationFn: async (alertId: number) => {
            await api.delete(`/alerts/${alertId}`);
        },
        onSuccess: (_, alertId) => {
            queryClient.setQueryData(['alert-notifications'], (oldData: Notification[] = []) => {
                return oldData.filter(notification => notification.id !== alertId);
            });
            queryClient.invalidateQueries({ queryKey: ['alert-stats'] });
        },
    })

    // clears all
    const clearAllNotificationsMutation = useMutation({
        mutationFn: async () => {
            await api.post('/alerts/clear-read');
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['alert-notifications'] });
            queryClient.invalidateQueries({ queryKey: ['alert-stats'] });
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