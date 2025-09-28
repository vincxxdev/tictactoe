import React, { useEffect, useState } from 'react';
import Game from './components/Game';
import { adjectives, nouns } from './utils/nameGenerator';

function App() {
  const [username, setUsername] = useState('');

  useEffect(() => {
    let storedName = localStorage.getItem('username');
    if (!storedName) {
      storedName = `${adjectives[Math.floor(Math.random() * adjectives.length)]} ${nouns[Math.floor(Math.random() * nouns.length)]}`;
      localStorage.setItem('username', storedName);
    }
    setUsername(storedName);
  }, []);

  return (
    <div className="App">
      <Game />
    </div>
  );
}

export default App;
