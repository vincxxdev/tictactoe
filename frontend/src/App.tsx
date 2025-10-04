import React, { useState } from 'react';
import Game from './components/Game';
import Lobby from './components/Lobby';
import UsernameSelection from './components/UsernameSelection';
import ErrorBoundary from './components/ErrorBoundary';
import { GameProvider, useGame } from './contexts/GameContext';
import { ToastProvider } from './contexts/ToastContext';

const AppContent: React.FC = () => {
  const { game, setPlayerLogin, playerLogin } = useGame();
  const [hasUsername, setHasUsername] = useState(false);

  const handleUsernameSelected = (username: string) => {
    setPlayerLogin(username);
    setHasUsername(true);
  };

  // Always require username selection if not set
  if (!hasUsername || !playerLogin) {
    return <UsernameSelection onUsernameSelected={handleUsernameSelected} />;
  }

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
