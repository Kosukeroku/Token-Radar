import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    private client: Client | null = null;
    private isWsActive = false;

    connect(onAlertReceived: (alert: any) => void) {
        const token = localStorage.getItem('token');
        if (!token) return;

        const wsUrl = `${window.location.protocol}//${window.location.host}/ws`;
        
        console.log('SockJS URL:', wsUrl);
        const socket = new SockJS(wsUrl);

        this.client = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                'Authorization': `Bearer ${token}`
            },
            reconnectDelay: 5000,
            debug: (str) => {
                console.log('STOMP:', str);
            },
            onConnect: () => {
                console.log('? SockJS connected');
                this.isWsActive = true;
                this.subscribeToUserAlerts(onAlertReceived);
            },
            onDisconnect: () => {
                console.log('?? SockJS disconnected');
                this.isWsActive = false;
            },
            onStompError: (error) => {
                console.error('SockJS error:', error);
            }
        });

        this.client.activate();
    }

    private subscribeToUserAlerts(callback: (alert: any) => void) {
        if (!this.client) return;

        this.client.subscribe(`/user/queue/alerts`, (message) => {
            try {
                console.log('?? Alert received via SockJS:', message.body);
                const alert = JSON.parse(message.body);
                callback(alert);
            } catch (error) {
                console.error('Parse error:', error);
            }
        });
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
            this.isWsActive = false;
        }
    }

    isConnected(): boolean {
        return this.isWsActive;
    }
}

export const webSocketService = new WebSocketService();