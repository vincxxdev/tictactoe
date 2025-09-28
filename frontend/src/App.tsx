import React, { useEffect } from 'react';
import Game from './components/Game';
import Lobby from './components/Lobby';
import { GameProvider, useGame } from './contexts/GameContext';
import { adjectives, nouns } from './utils/nameGenerator';

const AppContent: React.FC = () => {
  const { game, playerLogin, setPlayerLogin } = useGame();

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
    <GameProvider>
      <div className="App">
        <AppContent />
      </div>
    </GameProvider>
  );
}

export default App;
