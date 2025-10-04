import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import config from '../config/environment';

class SocketService {
    private client: Client;
    private connectionAttempts: number = 0;
    private maxReconnectAttempts: number = 10;

    constructor() {
        this.client = new Client({
            webSocketFactory: () => new SockJS(config.wsUrl),
            reconnectDelay: 5000,
            debug: (str) => {
                if (config.isDevelopment) {
                    console.log(new Date(), str);
                }
            },
        });
    }

    public connect(onConnectCallback: () => void, onErrorCallback?: (error: string) => void): void {
        this.client.onConnect = () => {
            console.log('Connected to WebSocket');
            this.connectionAttempts = 0;
            onConnectCallback();
        };

        this.client.onStompError = (frame) => {
            const errorMessage = 'Broker reported error: ' + frame.headers['message'];
            console.error(errorMessage);
            console.error('Additional details: ' + frame.body);
            
            if (onErrorCallback) {
                onErrorCallback(errorMessage);
            }
        };

        this.client.onWebSocketError = (error) => {
            this.connectionAttempts++;
            console.error('WebSocket error:', error);
            
            if (this.connectionAttempts >= this.maxReconnectAttempts && onErrorCallback) {
                onErrorCallback('Failed to connect after multiple attempts. Please refresh the page.');
            }
        };

        this.client.activate();
    }

    public disconnect(): void {
        this.client.deactivate();
        console.log('Disconnected from WebSocket');
    }

    public subscribe(topic: string, callback: (message: IMessage) => void): void {
        this.client.subscribe(topic, callback);
    }

    public sendMessage(destination: string, body: object): void {
        this.client.publish({ destination, body: JSON.stringify(body) });
    }
}

const socketService = new SocketService();
export default socketService;
