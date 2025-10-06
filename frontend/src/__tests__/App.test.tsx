import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import App from '../App';

// Mock socketService
jest.mock('../services/socketService', () => ({
  __esModule: true,
  default: {
    connect: jest.fn(),
    disconnect: jest.fn(),
    subscribe: jest.fn(),
    sendMessage: jest.fn(),
  },
}));

describe('App Component', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('renders App component', () => {
    render(<App />);
    expect(screen.getByText('Tic Tac Toe Online')).toBeInTheDocument();
  });

  test('shows username selection on app start', () => {
    render(<App />);
    
    expect(screen.getByText('Choose your username to start playing')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter your username')).toBeInTheDocument();
  });

  test('allows user to set username and proceed to lobby', async () => {
    render(<App />);
    
    // Username selection screen should be visible
    expect(screen.getByText('Choose your username to start playing')).toBeInTheDocument();
    
    const input = screen.getByPlaceholderText('Enter your username');
    fireEvent.change(input, { target: { value: 'NewPlayer' } });
    
    const continueButton = screen.getByRole('button', { name: /continue/i });
    fireEvent.click(continueButton);
    
    // Should now see the lobby
    await waitFor(() => {
      expect(screen.getByText('Create New Game')).toBeInTheDocument();
    });
    expect(screen.getByText(/NewPlayer/i)).toBeInTheDocument();
  });

  test('always requires username selection on fresh load', () => {
    render(<App />);
    
    // Should always show username selection first
    expect(screen.getByText('Choose your username to start playing')).toBeInTheDocument();
  });
});
