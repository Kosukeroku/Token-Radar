import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    private client: Client | null = null;
    private isWsActive = false;

    connect(onAlertReceived: (alert: any) => void) {
        const token = localStorage.getItem('token');
        if (!token) return;

        const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
        const socket = new SockJS(`${API_URL}/ws`);

        this.client = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                'Authorization': `Bearer ${token}`
            },
            reconnectDelay: 5000,
            debug: (str) => {
                if (str.includes('ERROR') || str.includes('CLOSED')) {
                    console.error('WebSocket:', str);
                }
            },
            onConnect: () => {
                this.isWsActive = true;
                this.subscribeToUserAlerts(onAlertReceived);
            },
            onDisconnect: () => {
                this.isWsActive = false;
            },
            onStompError: (error) => {
                console.error('WebSocket error:', error);
            }
        });

        this.client.activate();
    }

    private subscribeToUserAlerts(callback: (alert: any) => void) {
        if (!this.client) return;

        this.client.subscribe(`/user/queue/alerts`, (message) => {
            try {
                const alert = JSON.parse(message.body);
                callback(alert);
            } catch (error) {
                console.error('WebSocket parse error:', error);
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