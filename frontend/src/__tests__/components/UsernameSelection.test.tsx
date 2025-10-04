import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import UsernameSelection from '../../components/UsernameSelection';

describe('UsernameSelection', () => {
    let mockOnUsernameSelected: jest.Mock;

    beforeEach(() => {
        mockOnUsernameSelected = jest.fn();
        localStorage.clear();
    });

    test('renders username selection form', () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        expect(screen.getByText('Tic Tac Toe Online')).toBeInTheDocument();
        expect(screen.getByText('Choose your username to start playing')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Enter your username')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /continue/i })).toBeInTheDocument();
    });

    test('shows error when submitting empty username', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText('Please enter a username')).toBeInTheDocument();
        });
        
        expect(mockOnUsernameSelected).not.toHaveBeenCalled();
    });

    test('shows error when username is too short', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: 'a' } });
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText('Username must be at least 2 characters long')).toBeInTheDocument();
        });
        
        expect(mockOnUsernameSelected).not.toHaveBeenCalled();
    });

    test('shows error when username is too long', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: 'a'.repeat(21) } });
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText('Username must be less than 20 characters')).toBeInTheDocument();
        });
        
        expect(mockOnUsernameSelected).not.toHaveBeenCalled();
    });

    test('shows error when username contains invalid characters', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: 'user@name!' } });
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText('Username can only contain letters, numbers, spaces and underscores')).toBeInTheDocument();
        });
        
        expect(mockOnUsernameSelected).not.toHaveBeenCalled();
    });

    test('accepts valid username and calls callback', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: 'ValidUser123' } });
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(mockOnUsernameSelected).toHaveBeenCalledWith('ValidUser123');
        });
    });

    test('trims whitespace from username', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: '  ValidUser  ' } });
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(mockOnUsernameSelected).toHaveBeenCalledWith('ValidUser');
        });
    });

    test('accepts username with spaces and underscores', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: 'User Name_123' } });
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(mockOnUsernameSelected).toHaveBeenCalledWith('User Name_123');
        });
    });

    test('clears error when user types', async () => {
        render(<UsernameSelection onUsernameSelected={mockOnUsernameSelected} />);
        
        const submitButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText('Please enter a username')).toBeInTheDocument();
        });

        const input = screen.getByPlaceholderText('Enter your username');
        fireEvent.change(input, { target: { value: 'a' } });
        
        expect(screen.queryByText('Please enter a username')).not.toBeInTheDocument();
    });
});
