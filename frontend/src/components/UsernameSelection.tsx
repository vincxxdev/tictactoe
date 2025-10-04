import React, { useState } from 'react';

interface UsernameSelectionProps {
    onUsernameSelected: (username: string) => void;
}

const UsernameSelection: React.FC<UsernameSelectionProps> = ({ onUsernameSelected }) => {
    const [username, setUsername] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        
        // Username validation
        if (!username.trim()) {
            setError('Please enter a username');
            return;
        }
        
        if (username.trim().length < 2) {
            setError('Username must be at least 2 characters long');
            return;
        }
        
        if (username.trim().length > 20) {
            setError('Username must be less than 20 characters');
            return;
        }
        
        // Verify that it contains only letters, numbers, spaces and underscore
        if (!/^[a-zA-Z0-9_ ]+$/.test(username.trim())) {
            setError('Username can only contain letters, numbers, spaces and underscores');
            return;
        }
        
        const finalUsername = username.trim();
        onUsernameSelected(finalUsername);
    };

    return (
        <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center justify-center p-4">
            <div className="text-center mb-8">
                <h1 className="text-5xl font-bold mb-2">Tic Tac Toe Online</h1>
                <p className="text-xl text-gray-400">Choose your username to start playing</p>
            </div>

            <div className="w-full max-w-md bg-gray-800 p-8 rounded-lg shadow-lg">
                <form onSubmit={handleSubmit}>
                    <div className="mb-6">
                        <label htmlFor="username" className="block text-lg font-medium mb-2">
                            Username
                        </label>
                        <input
                            id="username"
                            type="text"
                            value={username}
                            onChange={(e) => {
                                setUsername(e.target.value);
                                setError('');
                            }}
                            placeholder="Enter your username"
                            className="w-full bg-gray-700 text-white placeholder-gray-400 p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 text-lg"
                            autoFocus
                            maxLength={20}
                        />
                        {error && (
                            <p className="text-red-400 text-sm mt-2">{error}</p>
                        )}
                        <p className="text-gray-400 text-xs mt-2">
                            2-20 characters. Letters, numbers, spaces and underscores only.
                        </p>
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-teal-500 hover:bg-teal-600 text-white font-bold py-3 px-4 rounded-lg text-xl transition duration-200"
                    >
                        Continue
                    </button>
                </form>
            </div>
        </div>
    );
};

export default UsernameSelection;
