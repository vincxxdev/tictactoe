import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import Board from '../../components/Board';

describe('Board Component', () => {
  const emptyBoard: ('X' | 'O' | null)[] = Array(9).fill(null);

  test('renders 9 squares', () => {
    const mockOnClick = vi.fn();
    render(<Board squares={emptyBoard} onClick={mockOnClick} />);
    
    const buttons = screen.getAllByRole('button');
    expect(buttons).toHaveLength(9);
  });

  test('renders squares with correct values', () => {
    const board: ('X' | 'O' | null)[] = ['X', 'O', 'X', null, null, null, null, null, null];
    const mockOnClick = vi.fn();
    render(<Board squares={board} onClick={mockOnClick} />);
    
    const buttons = screen.getAllByRole('button');
    expect(buttons[0]).toHaveTextContent('X');
    expect(buttons[1]).toHaveTextContent('O');
    expect(buttons[2]).toHaveTextContent('X');
    expect(buttons[3]).toHaveTextContent('');
  });

  test('calls onClick with correct index', () => {
    const mockOnClick = vi.fn();
    render(<Board squares={emptyBoard} onClick={mockOnClick} />);
    
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[4]); // Click middle square
    
    expect(mockOnClick).toHaveBeenCalledWith(4);
  });

  test('calls onClick for different squares', () => {
    const mockOnClick = vi.fn();
    render(<Board squares={emptyBoard} onClick={mockOnClick} />);
    
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[0]);
    fireEvent.click(buttons[8]);
    
    expect(mockOnClick).toHaveBeenCalledTimes(2);
    expect(mockOnClick).toHaveBeenNthCalledWith(1, 0);
    expect(mockOnClick).toHaveBeenNthCalledWith(2, 8);
  });

  test('applies correct grid layout classes', () => {
    const mockOnClick = vi.fn();
    render(<Board squares={emptyBoard} onClick={mockOnClick} />);
    
    const buttons = screen.getAllByRole('button');
    expect(buttons[0].parentElement).toHaveClass('grid', 'grid-cols-3', 'gap-4');
  });
});

