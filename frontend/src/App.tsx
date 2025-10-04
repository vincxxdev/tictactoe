import React, { useEffect } from 'react';
import Game from './components/Game';
import Lobby from './components/Lobby';
import ErrorBoundary from './components/ErrorBoundary';
import { GameProvider, useGame } from './contexts/GameContext';
import { ToastProvider } from './contexts/ToastContext';
import { adjectives, nouns } from './utils/nameGenerator';

const AppContent: React.FC = () => {
  const { game, setPlayerLogin } = useGame();

  useEffect(() => {
    let storedName = localStorage.getItem('username');
    if (!storedName) {
      storedName = `${adjectives[Math.floor(Math.random() * adjectives.length)]} ${nouns[Math.floor(Math.random() * nouns.length)]}`;
      localStorage.setItem('username', storedName);
    }
    setPlayerLogin(storedName);
  }, [setPlayerLogin]);

  return game ? <Game /> : <Lobby />;
}

function App() {
  return (
    <ErrorBoundary>
      <ToastProvider>
        <GameProvider>
          <div className="App">
            <AppContent />
          </div>
        </GameProvider>
      </ToastProvider>
    </ErrorBoundary>
  );
}

export default App;
