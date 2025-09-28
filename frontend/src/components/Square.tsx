import React from 'react';

interface SquareProps {
  value: 'X' | 'O' | null;
  onClick: () => void;
}

const Square: React.FC<SquareProps> = ({ value, onClick }) => {
  return (
    <button
      className="w-24 h-24 bg-gray-800 rounded-lg flex items-center justify-center text-4xl font-bold text-white focus:outline-none hover:bg-gray-700 transition duration-200"
      onClick={onClick}
    >
      {value}
    </button>
  );
};

export default Square;