import React from 'react';
import { renderHook, act, waitFor } from '@testing-library/react';
import { GameProvider, useGame } from '../../contexts/GameContext';
import socketService from '../../services/socketService';

// Mock socketService
jest.mock('../../services/socketService', () => ({
  __esModule: true,
  default: {
    connect: jest.fn(),
    disconnect: jest.fn(),
    subscribe: jest.fn(),
    sendMessage: jest.fn(),
  },
}));

describe('GameContext', () => {
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <GameProvider>{children}</GameProvider>
  );

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('provides initial state', () => {
    const { result } = renderHook(() => useGame(), { wrapper });

    expect(result.current.isConnected).toBe(false);
    expect(result.current.game).toBeNull();
    expect(result.current.playerLogin).toBe('');
  });

  test('throws error when useGame is used outside provider', () => {
    // Suppress console.error for this test
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => {
      renderHook(() => useGame());
    }).toThrow('useGame must be used within a GameProvider');

    consoleSpy.mockRestore();
  });

  test('setPlayerLogin updates player login', () => {
    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    expect(result.current.playerLogin).toBe('TestPlayer');
  });

  test('connects to socket when playerLogin is set', async () => {
    const mockConnect = socketService.connect as jest.Mock;
    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    await waitFor(() => {
      expect(mockConnect).toHaveBeenCalled();
    });
  });

  test('subscribes to player topics on connection', async () => {
    const mockConnect = socketService.connect as jest.Mock;
    const mockSubscribe = socketService.subscribe as jest.Mock;
    
    mockConnect.mockImplementation((callback) => {
      callback(); // Immediately call the callback to simulate connection
    });

    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalledWith(
        '/topic/game.connected/TestPlayer',
        expect.any(Function)
      );
    });
    
    expect(mockSubscribe).toHaveBeenCalledWith(
      '/topic/game.created/TestPlayer',
      expect.any(Function)
    );
  });

  test('createGame sends correct message', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.createGame();
    });

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.start', {
      login: 'TestPlayer',
    });
  });

  test('connectToRandomGame sends correct message', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.connectToRandomGame();
    });

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.connect', {
      player: { login: 'TestPlayer' },
    });
  });

  test('connectToGameById sends correct message with gameId', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.connectToGameById('test-game-id');
    });

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.connect', {
      player: { login: 'TestPlayer' },
      gameId: 'test-game-id',
    });
  });

  test('makeMove sends correct message', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const mockConnect = socketService.connect as jest.Mock;
    const mockSubscribe = socketService.subscribe as jest.Mock;

    mockConnect.mockImplementation((callback) => {
      callback();
    });

    mockSubscribe.mockImplementation((topic, callback) => {
      if (topic === '/topic/game.created/TestPlayer') {
        // Simulate game creation
        callback({
          body: JSON.stringify({
            gameId: 'test-game-id',
            board: Array(9).fill(null),
            player1: { login: 'TestPlayer' },
            player2: null,
            status: 'NEW',
            winner: null,
            currentPlayerLogin: null,
            surrenderRequesterLogin: null,
          }),
        });
      }
    });

    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.createGame();
    });

    act(() => {
      result.current.makeMove(4);
    });

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.gameplay', {
      playerLogin: 'TestPlayer',
      squareIndex: 4,
      gameId: 'test-game-id',
    });
  });

  test('requestSurrender sends correct message', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const mockConnect = socketService.connect as jest.Mock;
    const mockSubscribe = socketService.subscribe as jest.Mock;

    mockConnect.mockImplementation((callback) => {
      callback();
    });

    mockSubscribe.mockImplementation((topic, callback) => {
      if (topic === '/topic/game.created/TestPlayer') {
        callback({
          body: JSON.stringify({
            gameId: 'test-game-id',
            board: Array(9).fill(null),
            player1: { login: 'TestPlayer' },
            player2: null,
            status: 'NEW',
            winner: null,
            currentPlayerLogin: null,
            surrenderRequesterLogin: null,
          }),
        });
      }
    });

    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.createGame();
    });

    result.current.requestSurrender();

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.surrender', {
      playerLogin: 'TestPlayer',
      gameId: 'test-game-id',
    });
  });

  test('respondToSurrender sends correct message with accepted true', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const mockConnect = socketService.connect as jest.Mock;
    const mockSubscribe = socketService.subscribe as jest.Mock;

    mockConnect.mockImplementation((callback) => {
      callback();
    });

    mockSubscribe.mockImplementation((topic, callback) => {
      if (topic === '/topic/game.created/TestPlayer') {
        callback({
          body: JSON.stringify({
            gameId: 'test-game-id',
            board: Array(9).fill(null),
            player1: { login: 'TestPlayer' },
            player2: null,
            status: 'NEW',
            winner: null,
            currentPlayerLogin: null,
            surrenderRequesterLogin: null,
          }),
        });
      }
    });

    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.createGame();
    });

    result.current.respondToSurrender(true);

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.surrender.response', {
      playerLogin: 'TestPlayer',
      gameId: 'test-game-id',
      accepted: true,
    });
  });

  test('respondToSurrender sends correct message with accepted false', () => {
    const mockSendMessage = socketService.sendMessage as jest.Mock;
    const mockConnect = socketService.connect as jest.Mock;
    const mockSubscribe = socketService.subscribe as jest.Mock;

    mockConnect.mockImplementation((callback) => {
      callback();
    });

    mockSubscribe.mockImplementation((topic, callback) => {
      if (topic === '/topic/game.created/TestPlayer') {
        callback({
          body: JSON.stringify({
            gameId: 'test-game-id',
            board: Array(9).fill(null),
            player1: { login: 'TestPlayer' },
            player2: null,
            status: 'NEW',
            winner: null,
            currentPlayerLogin: null,
            surrenderRequesterLogin: null,
          }),
        });
      }
    });

    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    act(() => {
      result.current.createGame();
    });

    result.current.respondToSurrender(false);

    expect(mockSendMessage).toHaveBeenCalledWith('/app/game.surrender.response', {
      playerLogin: 'TestPlayer',
      gameId: 'test-game-id',
      accepted: false,
    });
  });

  test('disconnects socket on unmount', () => {
    const mockDisconnect = socketService.disconnect as jest.Mock;
    const { result, unmount } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    unmount();

    expect(mockDisconnect).toHaveBeenCalled();
  });

  test('updates game state when receiving game data', async () => {
    const mockConnect = socketService.connect as jest.Mock;
    const mockSubscribe = socketService.subscribe as jest.Mock;

    mockConnect.mockImplementation((callback) => {
      callback();
    });

    const mockGameData = {
      gameId: 'test-game-id',
      board: Array(9).fill(null),
      player1: { login: 'TestPlayer' },
      player2: { login: 'Player2' },
      status: 'IN_PROGRESS',
      winner: null,
      currentPlayerLogin: 'TestPlayer',
      surrenderRequesterLogin: null,
    };

    mockSubscribe.mockImplementation((topic, callback) => {
      if (topic === '/topic/game.connected/TestPlayer') {
        callback({
          body: JSON.stringify(mockGameData),
        });
      }
    });

    const { result } = renderHook(() => useGame(), { wrapper });

    act(() => {
      result.current.setPlayerLogin('TestPlayer');
    });

    await waitFor(() => {
      expect(result.current.game).toEqual(mockGameData);
    });
  });
});

