import React from 'react';

interface SquareProps {
  // Props for the square will go here
}

const Square: React.FC<SquareProps> = () => {
  return (
    <div className="w-24 h-24 bg-gray-800 rounded-lg flex items-center justify-center text-4xl font-bold">
      {/* X or O will go here */}
    </div>
  );
};

export default Square;
