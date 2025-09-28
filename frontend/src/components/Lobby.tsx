import React, { useState } from 'react';
import { useGame } from '../contexts/GameContext';

const Lobby: React.FC = () => {
    const { playerLogin, createGame, connectToGameById, connectToRandomGame } = useGame();
    const [gameIdInput, setGameIdInput] = useState('');

    return (
        <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center justify-center p-4">
            <div className="text-center">
                <h1 className="text-5xl font-bold mb-2">Tic Tac Toe Online</h1>
                <p className="text-xl mb-8">Welcome, <span className='font-bold text-teal-400'>{playerLogin}</span></p>
            </div>

            <div className="w-full max-w-sm bg-gray-800 p-8 rounded-lg shadow-lg">
                <button
                    onClick={createGame}
                    className="w-full bg-teal-500 hover:bg-teal-600 text-white font-bold py-3 px-4 rounded-lg text-xl transition duration-200 mb-4"
                >
                    Create New Game
                </button>

                <button
                    onClick={connectToRandomGame}
                    className="w-full bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-4 rounded-lg text-xl transition duration-200 mb-4"
                >
                    Join Random Game
                </button>
                
                <div className="my-2 text-center text-gray-400">OR</div>

                <div className="flex flex-col">
                    <input
                        type="text"
                        value={gameIdInput}
                        onChange={(e) => setGameIdInput(e.target.value)}
                        placeholder="Enter Game ID"
                        className="bg-gray-700 text-white placeholder-gray-400 text-center p-3 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                    <button
                        onClick={() => connectToGameById(gameIdInput)}
                        disabled={!gameIdInput}
                        className="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-4 rounded-lg text-xl transition duration-200 disabled:bg-gray-600"
                    >
                        Join Game by ID
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Lobby;