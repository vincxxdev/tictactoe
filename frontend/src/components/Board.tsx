import React from 'react';
import Square from './Square';

interface BoardProps {
  squares: ('X' | 'O' | null)[];
  onClick: (i: number) => void;
}

const Board: React.FC<BoardProps> = ({ squares, onClick }) => {
  return (
    <div className="grid grid-cols-3 gap-4 p-4 bg-gray-900 rounded-lg">
      {squares.map((square, i) => (
        <Square key={i} value={square} onClick={() => onClick(i)} />
      ))}
    </div>
  );
};

export default Board;