import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = 'http://localhost:8080/ws';

class SocketService {
    private client: Client;

    constructor() {
        this.client = new Client({
            webSocketFactory: () => new SockJS(SOCKET_URL),
            reconnectDelay: 5000,
            debug: (str) => {
                console.log(new Date(), str);
            },
        });
    }

    public connect(onConnectCallback: () => void): void {
        this.client.onConnect = () => {
            console.log('Connected to WebSocket');
            onConnectCallback();
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
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
