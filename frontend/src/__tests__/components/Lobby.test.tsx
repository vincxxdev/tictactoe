import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import Lobby from '../../components/Lobby';
import { GameProvider } from '../../contexts/GameContext';

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

describe('Lobby Component', () => {
  const renderLobby = () => {
    return render(
      <GameProvider>
        <Lobby />
      </GameProvider>
    );
  };

  beforeEach(() => {
    localStorage.setItem('username', 'TestPlayer');
  });

  afterEach(() => {
    localStorage.clear();
  });

  test('renders lobby with title', () => {
    renderLobby();
    
    expect(screen.getByText('Tic Tac Toe Online')).toBeInTheDocument();
  });

  test('displays player name from context', () => {
    renderLobby();
    
    // The player name should be rendered after context sets it
    expect(screen.getByText(/Welcome,/i)).toBeInTheDocument();
  });

  test('renders create game button', () => {
    renderLobby();
    
    const createButton = screen.getByText('Create New Game');
    expect(createButton).toBeInTheDocument();
  });

  test('renders join random game button', () => {
    renderLobby();
    
    const joinButton = screen.getByText('Join Random Game');
    expect(joinButton).toBeInTheDocument();
  });

  test('renders game ID input field', () => {
    renderLobby();
    
    const input = screen.getByPlaceholderText('Enter Game ID');
    expect(input).toBeInTheDocument();
  });

  test('renders join by ID button', () => {
    renderLobby();
    
    const joinByIdButton = screen.getByText('Join Game by ID');
    expect(joinByIdButton).toBeInTheDocument();
  });

  test('join by ID button is disabled when input is empty', () => {
    renderLobby();
    
    const button = screen.getByText('Join Game by ID');
    expect(button).toBeDisabled();
  });

  test('join by ID button is enabled when input has value', () => {
    renderLobby();
    
    const input = screen.getByPlaceholderText('Enter Game ID');
    const button = screen.getByText('Join Game by ID');
    
    fireEvent.change(input, { target: { value: 'game-123' } });
    
    expect(button).not.toBeDisabled();
  });

  test('updates input value on change', () => {
    renderLobby();
    
    const input = screen.getByPlaceholderText('Enter Game ID') as HTMLInputElement;
    
    fireEvent.change(input, { target: { value: 'test-game-id' } });
    
    expect(input.value).toBe('test-game-id');
  });

  test('displays OR separator', () => {
    renderLobby();
    
    expect(screen.getByText('OR')).toBeInTheDocument();
  });
});

