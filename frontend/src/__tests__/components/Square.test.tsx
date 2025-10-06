import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import Square from '../../components/Square';

describe('Square Component', () => {
  test('renders empty square', () => {
    const mockOnClick = vi.fn();
    render(<Square value={null} onClick={mockOnClick} />);
    
    const button = screen.getByRole('button');
    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent('');
  });

  test('renders square with X', () => {
    const mockOnClick = vi.fn();
    render(<Square value="X" onClick={mockOnClick} />);
    
    const button = screen.getByRole('button');
    expect(button).toHaveTextContent('X');
  });

  test('renders square with O', () => {
    const mockOnClick = vi.fn();
    render(<Square value="O" onClick={mockOnClick} />);
    
    const button = screen.getByRole('button');
    expect(button).toHaveTextContent('O');
  });

  test('calls onClick when clicked', () => {
    const mockOnClick = vi.fn();
    render(<Square value={null} onClick={mockOnClick} />);
    
    const button = screen.getByRole('button');
    fireEvent.click(button);
    
    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });

  test('applies correct CSS classes', () => {
    const mockOnClick = vi.fn();
    render(<Square value={null} onClick={mockOnClick} />);
    
    const button = screen.getByRole('button');
    expect(button).toHaveClass('w-24', 'h-24', 'bg-gray-800', 'rounded-lg');
  });
});

