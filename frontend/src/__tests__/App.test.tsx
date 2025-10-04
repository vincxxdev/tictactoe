import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
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

  test('generates and stores username if not present', async () => {
    render(<App />);
    
    await waitFor(() => {
      expect(localStorage.getItem('username')).toBeTruthy();
    });
    
    const storedUsername = localStorage.getItem('username');
    expect(storedUsername).toMatch(/^[A-Z][a-z]+ [A-Z][a-z]+$/);
  });

  test('uses existing username from localStorage', async () => {
    localStorage.setItem('username', 'TestPlayer User');
    
    render(<App />);
    
    await waitFor(() => {
      expect(screen.getByText(/TestPlayer User/i)).toBeInTheDocument();
    });
  });

  test('renders lobby by default when no game', () => {
    render(<App />);
    
    expect(screen.getByText('Create New Game')).toBeInTheDocument();
    expect(screen.getByText('Join Random Game')).toBeInTheDocument();
  });
});
