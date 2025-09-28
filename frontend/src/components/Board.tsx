import React from 'react';

interface BoardProps {
  // Props for the board will go here
}

const Board: React.FC<BoardProps> = () => {
  return (
    <div className="grid grid-cols-3 gap-4">
      {/* Squares will be rendered here */}
    </div>
  );
};

export default Board;
