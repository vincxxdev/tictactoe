import { render, screen, fireEvent } from '@testing-library/react';
import { vi, Mock } from 'vitest';

import Game from '../../components/Game';
import * as GameContext from '../../contexts/GameContext';

// Mock socketService
vi.mock('../../services/socketService', () => ({
  default: {
    connect: vi.fn(),
    disconnect: vi.fn(),
    subscribe: vi.fn(),
    sendMessage: vi.fn(),
  },
}));

describe('Game Component', () => {
  const mockGame = {
    gameId: 'test-game-id',
    board: Array(9).fill(null) as ('X' | 'O' | null)[],
    currentPlayerLogin: 'Player1',
    player1: { login: 'Player1' },
    player2: { login: 'Player2' },
    status: 'IN_PROGRESS' as const,
    winner: null,
    surrenderRequesterLogin: null,
    pendingJoinPlayer: null,
    rematchRequesterLogin: null,
  };

  const mockUseGame = {
    isConnected: true,
    game: mockGame,
    playerLogin: 'Player1',
    setPlayerLogin: vi.fn(),
    createGame: vi.fn(),
    connectToRandomGame: vi.fn(),
    connectToGameById: vi.fn(),
    makeMove: vi.fn(),
    requestSurrender: vi.fn(),
    respondToSurrender: vi.fn(),
    joinPending: false,
    respondToJoinRequest: vi.fn(),
    requestRematch: vi.fn(),
    respondToRematch: vi.fn(),
    returnToLobby: vi.fn(),
  };

  beforeEach(() => {
    vi.spyOn(GameContext, 'useGame').mockReturnValue(mockUseGame);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  test('renders game board', () => {
    render(<Game />);
    
    const buttons = screen.getAllByRole('button');
    // 9 square buttons + surrender button
    expect(buttons.length).toBeGreaterThanOrEqual(9);
  });

  test('displays player symbols', () => {
    render(<Game />);
    
    expect(screen.getByText('You:')).toBeInTheDocument();
    expect(screen.getByText('Opponent:')).toBeInTheDocument();
  });

  test('displays "Your Turn" when it is player turn', () => {
    render(<Game />);
    
    expect(screen.getByText('Your Turn')).toBeInTheDocument();
  });

  test('displays "Opponent\'s Turn" when it is not player turn', () => {
    const gameWithOpponentTurn = {
      ...mockGame,
      currentPlayerLogin: 'Player2',
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: gameWithOpponentTurn,
    });

    render(<Game />);
    
    expect(screen.getByText("Opponent's Turn")).toBeInTheDocument();
  });

  test('displays winner message when game is finished', () => {
    const finishedGame = {
      ...mockGame,
      status: 'FINISHED' as const,
      winner: 'X' as const,
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: finishedGame,
    });

    render(<Game />);
    
    expect(screen.getByText('You Won!')).toBeInTheDocument();
  });

  test('displays loser message when opponent wins', () => {
    const finishedGame = {
      ...mockGame,
      status: 'FINISHED' as const,
      winner: 'O' as const,
      currentPlayerLogin: 'Player2',
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: finishedGame,
    });

    render(<Game />);
    
    expect(screen.getByText('You Lost!')).toBeInTheDocument();
  });

  test('displays draw message when game is draw', () => {
    const drawGame = {
      ...mockGame,
      status: 'FINISHED' as const,
      winner: null,
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: drawGame,
    });

    render(<Game />);
    
    expect(screen.getByText("It's a Draw!")).toBeInTheDocument();
  });

  test('calls makeMove when square is clicked during player turn', () => {
    const makeMoveMock = vi.fn();
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      makeMove: makeMoveMock,
    });

    render(<Game />);
    
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[0]); // Click first square
    
    expect(makeMoveMock).toHaveBeenCalledWith(0);
  });

  test('does not call makeMove when it is not player turn', () => {
    const makeMoveMock = vi.fn();
    const gameWithOpponentTurn = {
      ...mockGame,
      currentPlayerLogin: 'Player2',
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: gameWithOpponentTurn,
      makeMove: makeMoveMock,
    });

    render(<Game />);
    
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[0]);
    
    expect(makeMoveMock).not.toHaveBeenCalled();
  });

  test('does not call makeMove when square is occupied', () => {
    const makeMoveMock = vi.fn();
    const gameWithMove = {
      ...mockGame,
      board: ['X', null, null, null, null, null, null, null, null] as ('X' | 'O' | null)[],
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: gameWithMove,
      makeMove: makeMoveMock,
    });

    render(<Game />);
    
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[0]); // Click occupied square
    
    expect(makeMoveMock).not.toHaveBeenCalled();
  });

  test('renders surrender button during game', () => {
    render(<Game />);
    
    expect(screen.getByText('Surrender')).toBeInTheDocument();
  });

  test('calls requestSurrender when surrender button is clicked', () => {
    const requestSurrenderMock = vi.fn();
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      requestSurrender: requestSurrenderMock,
    });

    render(<Game />);
    
    const surrenderButton = screen.getByText('Surrender');
    fireEvent.click(surrenderButton);
    
    expect(requestSurrenderMock).toHaveBeenCalled();
  });

  test('displays surrender dialog when opponent requests surrender', () => {
    const gameWithSurrenderRequest = {
      ...mockGame,
      surrenderRequesterLogin: 'Player2',
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: gameWithSurrenderRequest,
    });

    render(<Game />);
    
    expect(screen.getByText('Opponent wants to surrender')).toBeInTheDocument();
    expect(screen.getByText('Do you accept?')).toBeInTheDocument();
  });

  test('calls respondToSurrender with true when accept is clicked', () => {
    const respondToSurrenderMock = vi.fn();
    const gameWithSurrenderRequest = {
      ...mockGame,
      surrenderRequesterLogin: 'Player2',
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: gameWithSurrenderRequest,
      respondToSurrender: respondToSurrenderMock,
    });

    render(<Game />);
    
    const acceptButton = screen.getByText('Accept');
    fireEvent.click(acceptButton);
    
    expect(respondToSurrenderMock).toHaveBeenCalledWith(true);
  });

  test('calls respondToSurrender with false when decline is clicked', () => {
    const respondToSurrenderMock = vi.fn();
    const gameWithSurrenderRequest = {
      ...mockGame,
      surrenderRequesterLogin: 'Player2',
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: gameWithSurrenderRequest,
      respondToSurrender: respondToSurrenderMock,
    });

    render(<Game />);
    
    const declineButton = screen.getByText('Decline');
    fireEvent.click(declineButton);
    
    expect(respondToSurrenderMock).toHaveBeenCalledWith(false);
  });

  test('displays game ID', () => {
    render(<Game />);
    
    expect(screen.getByText(/Game ID: test-game-id/i)).toBeInTheDocument();
  });

  test('displays "Play Again" button when game is finished', () => {
    const finishedGame = {
      ...mockGame,
      status: 'FINISHED' as const,
      winner: 'X' as const,
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: finishedGame,
    });

    render(<Game />);
    
    expect(screen.getByText('Return to Lobby')).toBeInTheDocument();
    expect(screen.getByText('Rematch')).toBeInTheDocument();
  });

  test('displays waiting message when player2 is null', () => {
    const newGame = {
      ...mockGame,
      player2: null,
      status: 'NEW' as const,
    };
    vi.spyOn(GameContext, 'useGame').mockReturnValue({
      ...mockUseGame,
      game: newGame,
    });

    render(<Game />);
    
    expect(screen.getByText('Waiting for opponent to join...')).toBeInTheDocument();
  });
});

