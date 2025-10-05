import React, { createContext, useContext, useState, ReactNode, useEffect, useCallback } from 'react';
import socketService from '../services/socketService';
import { IMessage } from '@stomp/stompjs';

// Types definition for the game state and the context
interface GameState {
    gameId: string;
    board: ('X' | 'O' | null)[];
    currentPlayerLogin: string;
    player1: { login: string };
    player2: { login: string } | null;
    status: 'NEW' | 'IN_PROGRESS' | 'FINISHED';
    winner: 'X' | 'O' | null;
    surrenderRequesterLogin: string | null;
    pendingJoinPlayer: { login: string } | null;
    rematchRequesterLogin: string | null;
}

interface GameContextType {
    isConnected: boolean;
    game: GameState | null;
    playerLogin: string;
    joinPending: boolean;
    setPlayerLogin: (login: string) => void;
    createGame: () => void;
    connectToRandomGame: () => void;
    connectToGameById: (gameId: string) => void;
    makeMove: (index: number) => void;
    requestSurrender: () => void;
    respondToSurrender: (accepted: boolean) => void;
    respondToJoinRequest: (requesterLogin: string, accepted: boolean) => void;
    requestRematch: () => void;
    respondToRematch: (accepted: boolean) => void;
    returnToLobby: () => void;
}

const GameContext = createContext<GameContextType | undefined>(undefined);

export const GameProvider = ({ children }: { children: ReactNode }) => {
    const [isConnected, setIsConnected] = useState(false);
    const [game, setGame] = useState<GameState | null>(null);
    const [playerLogin, setPlayerLogin] = useState('');
    const [joinPending, setJoinPending] = useState(false);

    const handleGameUpdate = useCallback((message: IMessage) => {
        const gameData = JSON.parse(message.body);
        setGame(gameData);
    }, []);

    const subscribeToGameTopic = useCallback((gameId: string) => {
        socketService.subscribe(`/topic/game.${gameId}`, handleGameUpdate);
    }, [handleGameUpdate]);

    useEffect(() => {
        if (playerLogin) {
            socketService.connect(() => {
                setIsConnected(true);
                // Subscribe to the personal channel for game connection events
                socketService.subscribe(`/topic/game.connected/${playerLogin}`, (message) => {
                    const gameData = JSON.parse(message.body);
                    setGame(gameData);
                    setJoinPending(false);
                    subscribeToGameTopic(gameData.gameId);
                });
                // Subscribe to the personal channel for game creation events
                socketService.subscribe(`/topic/game.created/${playerLogin}`, (message) => {
                    const gameData = JSON.parse(message.body);
                    setGame(gameData);
                    subscribeToGameTopic(gameData.gameId);
                });
                // Subscribe to join request notifications (for game creator)
                socketService.subscribe(`/topic/game.join.request/${playerLogin}`, (message) => {
                    const gameData = JSON.parse(message.body);
                    setGame(gameData);
                });
                // Subscribe to join pending notifications (for joining player)
                socketService.subscribe(`/topic/game.join.pending/${playerLogin}`, (message) => {
                    const gameData = JSON.parse(message.body);
                    setGame(gameData);
                    setJoinPending(true);
                });
                // Subscribe to join rejected notifications
                socketService.subscribe(`/topic/game.join.rejected/${playerLogin}`, (message) => {
                    setJoinPending(false);
                    setGame(null);
                    alert('Your join request was rejected by the game creator.');
                });
                // Subscribe to game updates
                socketService.subscribe(`/topic/game.updated/${playerLogin}`, (message) => {
                    const gameData = JSON.parse(message.body);
                    setGame(gameData);
                });
                // Subscribe to rematch accepted notifications (new game created)
                socketService.subscribe(`/topic/game.rematch.accepted/${playerLogin}`, (message) => {
                    const gameData = JSON.parse(message.body);
                    setGame(gameData);
                    subscribeToGameTopic(gameData.gameId);
                });
            });

            return () => {
                socketService.disconnect();
                setIsConnected(false);
            };
        }
    }, [playerLogin, subscribeToGameTopic]);

    const createGame = () => {
        socketService.sendMessage('/app/game.start', { login: playerLogin });
    };

    const connectToRandomGame = () => {
        socketService.sendMessage('/app/game.connect', { player: { login: playerLogin } });
    };

    const connectToGameById = (gameId: string) => {
        socketService.sendMessage('/app/game.connect', { player: { login: playerLogin }, gameId });
    };

    const makeMove = (squareIndex: number) => {
        if (game) {
            socketService.sendMessage('/app/game.gameplay', { 
                playerLogin,
                squareIndex,
                gameId: game.gameId 
            });
        }
    };

    const requestSurrender = () => {
        if (game) {
            socketService.sendMessage('/app/game.surrender', { playerLogin, gameId: game.gameId });
        }
    };

    const respondToSurrender = (accepted: boolean) => {
        if (game) {
            socketService.sendMessage('/app/game.surrender.response', { playerLogin, gameId: game.gameId, accepted });
        }
    };

    const respondToJoinRequest = (requesterLogin: string, accepted: boolean) => {
        if (game) {
            socketService.sendMessage('/app/game.join.response', { 
                responderLogin: playerLogin, 
                requesterLogin, 
                gameId: game.gameId, 
                accepted 
            });
        }
    };

    const requestRematch = () => {
        if (game) {
            socketService.sendMessage('/app/game.rematch', { playerLogin, gameId: game.gameId });
        }
    };

    const respondToRematch = (accepted: boolean) => {
        if (game) {
            socketService.sendMessage('/app/game.rematch.response', { playerLogin, gameId: game.gameId, accepted });
        }
    };

    const returnToLobby = () => {
        setGame(null);
        setJoinPending(false);
    };

    return (
        <GameContext.Provider value={{ 
            isConnected, 
            game, 
            playerLogin, 
            joinPending,
            setPlayerLogin, 
            createGame, 
            connectToRandomGame, 
            connectToGameById, 
            makeMove, 
            requestSurrender, 
            respondToSurrender,
            respondToJoinRequest,
            requestRematch,
            respondToRematch,
            returnToLobby
        }}>
            {children}
        </GameContext.Provider>
    );
};

export const useGame = () => {
    const context = useContext(GameContext);
    if (context === undefined) {
        throw new Error('useGame must be used within a GameProvider');
    }
    return context;
};