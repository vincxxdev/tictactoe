import React, { useState, useEffect } from 'react';
import { useGame } from '../contexts/GameContext';
import config from '../config/environment';

interface AvailableGame {
    gameId: string;
    player1: { login: string };
    status: string;
    createdAt: string;
}

interface AvailableGamesProps {
    onBack: () => void;
}

const AvailableGames: React.FC<AvailableGamesProps> = ({ onBack }) => {
    const { connectToGameById } = useGame();
    const [games, setGames] = useState<AvailableGame[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchAvailableGames = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${config.apiUrl}/api/games/available`);
            if (!response.ok) {
                throw new Error('Failed to fetch games');
            }
            const data = await response.json();
            setGames(data);
        } catch (err) {
            setError('Error loading available games');
            console.error('Error fetching games:', err);
        } finally {
            setLoading(false);
        }
    };

    const getTimeAgo = (createdAt: string) => {
        if (!createdAt) return '';
        const now = new Date();
        const created = new Date(createdAt);
        const diffMs = now.getTime() - created.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        
        if (diffMins < 1) return 'just now';
        if (diffMins === 1) return '1 minute ago';
        if (diffMins < 60) return `${diffMins} minutes ago`;
        
        const diffHours = Math.floor(diffMins / 60);
        if (diffHours === 1) return '1 hour ago';
        return `${diffHours} hours ago`;
    };

    useEffect(() => {
        fetchAvailableGames();
    }, []);

    const handleJoinGame = (gameId: string) => {
        connectToGameById(gameId);
    };

    return (
        <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center justify-center p-4">
            <div className="w-full max-w-2xl">
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-3xl font-bold">Available Games</h2>
                    <button
                        onClick={onBack}
                        className="bg-gray-700 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded-lg transition duration-200"
                    >
                        Back to Lobby
                    </button>
                </div>

                {loading && (
                    <div className="text-center py-8">
                        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-teal-500"></div>
                        <p className="mt-2 text-gray-400">Loading available games...</p>
                    </div>
                )}

                {error && (
                    <div className="bg-red-900 bg-opacity-50 border border-red-500 text-red-200 px-4 py-3 rounded-lg mb-4">
                        {error}
                    </div>
                )}

                {!loading && !error && games.length === 0 && (
                    <div className="bg-gray-800 p-8 rounded-lg text-center">
                        <p className="text-xl text-gray-400">No available games at the moment</p>
                        <p className="text-sm text-gray-500 mt-2">Create a new game or try again later</p>
                    </div>
                )}

                {!loading && !error && games.length > 0 && (
                    <div className="space-y-3">
                        {games.map((game) => (
                            <div
                                key={game.gameId}
                                className="bg-gray-800 p-4 rounded-lg shadow-lg flex items-center justify-between hover:bg-gray-750 transition duration-200"
                            >
                                <div className="flex-1">
                                    <p className="text-lg font-semibold text-teal-400">
                                        {game.player1.login}'s Game
                                    </p>
                                    <p className="text-sm text-gray-400">
                                        Game ID: <span className="font-mono">{game.gameId.substring(0, 8)}...</span>
                                    </p>
                                    {game.createdAt && (
                                        <p className="text-xs text-gray-500 mt-1">
                                            Created {getTimeAgo(game.createdAt)}
                                        </p>
                                    )}
                                </div>
                                <button
                                    onClick={() => handleJoinGame(game.gameId)}
                                    className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-6 rounded-lg transition duration-200"
                                >
                                    Join
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                <div className="mt-4 text-center">
                    <button
                        onClick={fetchAvailableGames}
                        disabled={loading}
                        className="text-teal-400 hover:text-teal-300 underline disabled:text-gray-500 disabled:no-underline"
                    >
                        {loading ? 'Refreshing...' : 'Refresh List'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AvailableGames;
