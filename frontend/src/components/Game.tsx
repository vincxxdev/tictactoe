import React from 'react';
import Board from './Board';
import { useGame } from '../contexts/GameContext';

const Game: React.FC = () => {
  const { game, playerLogin, joinPending, makeMove, requestSurrender, respondToSurrender, respondToJoinRequest } = useGame();

  if (!game) {
    return <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">Loading...</div>;
  }

  const { board, winner, status, currentPlayerLogin, player1, player2, surrenderRequesterLogin, pendingJoinPlayer } = game;

  const isMyTurn = currentPlayerLogin === playerLogin;
  const isGameInProgress = status === 'IN_PROGRESS';

  const handleSquareClick = (i: number) => {
    if (!isGameInProgress || board[i] || !isMyTurn || surrenderRequesterLogin) {
      return; // Not your turn, game finished, cell occupied or surrender pending
    }
    makeMove(i);
  };

  const getPlayerSymbol = (login: string | null) => {
    if (!login) return null;
    if (player1.login === login) return 'X';
    if (player2 && player2.login === login) return 'O';
    return null;
  };

  const renderStatus = () => {
    if (status === 'FINISHED') {
      if (winner) {
        const winnerLogin = winner === 'X' ? player1.login : player2?.login;
        return winnerLogin === playerLogin ? "You Won!" : "You Lost!";
      }
      return "It's a Draw!";
    }
    if (status === 'NEW') {
        if (joinPending) {
            return "Waiting for game creator to accept...";
        }
        if (pendingJoinPlayer) {
            return "Player wants to join...";
        }
        return "Waiting for opponent to join...";
    }
    if (surrenderRequesterLogin) {
        return "Surrender request pending...";
    }
    return isMyTurn ? "Your Turn" : "Opponent's Turn";
  };

  const renderSurrenderDialog = () => {
    if (!surrenderRequesterLogin || surrenderRequesterLogin === playerLogin) {
        return null;
    }
    return (
        <div className="absolute inset-0 bg-black bg-opacity-70 flex flex-col items-center justify-center">
            <h2 className='text-3xl font-bold mb-4'>Opponent wants to surrender</h2>
            <p className='text-xl mb-6'>Do you accept?</p>
            <div className='flex gap-x-4'>
                <button onClick={() => respondToSurrender(true)} className='bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-8 rounded-lg text-xl'>Accept</button>
                <button onClick={() => respondToSurrender(false)} className='bg-red-500 hover:bg-red-600 text-white font-bold py-3 px-8 rounded-lg text-xl'>Decline</button>
            </div>
        </div>
    )
  }

  const renderJoinRequestDialog = () => {
    if (!pendingJoinPlayer || playerLogin !== player1.login) {
        return null;
    }
    return (
        <div className="absolute inset-0 bg-black bg-opacity-70 flex flex-col items-center justify-center z-10">
            <h2 className='text-3xl font-bold mb-4'>Player wants to join</h2>
            <p className='text-xl mb-2'>{pendingJoinPlayer.login}</p>
            <p className='text-lg mb-6'>Do you accept this player?</p>
            <div className='flex gap-x-4'>
                <button onClick={() => respondToJoinRequest(pendingJoinPlayer.login, true)} className='bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-8 rounded-lg text-xl'>Accept</button>
                <button onClick={() => respondToJoinRequest(pendingJoinPlayer.login, false)} className='bg-red-500 hover:bg-red-600 text-white font-bold py-3 px-8 rounded-lg text-xl'>Reject</button>
            </div>
        </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center justify-center p-4 relative">
      <div className="w-full max-w-md mx-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl">You: <span className='font-bold text-teal-400'>{getPlayerSymbol(playerLogin)}</span></h2>
          <h2 className="text-xl">Opponent: <span className='font-bold text-red-400'>{getPlayerSymbol(playerLogin === player1.login ? player2?.login || null : player1.login)}</span></h2>
        </div>
        <div className="mb-4 text-2xl text-center font-bold h-8">
          {renderStatus()}
        </div>
        <Board squares={board} onClick={handleSquareClick} />
        <div className="mt-6 text-center">
            {isGameInProgress && !surrenderRequesterLogin && (
                <button onClick={requestSurrender} className='bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-6 rounded-lg'>
                    Surrender
                </button>
            )}
            {surrenderRequesterLogin === playerLogin && (
                <p className='text-yellow-400'>Surrender request sent...</p>
            )}
            {status === 'FINISHED' && (
                <a href='/' className='bg-teal-500 hover:bg-teal-600 text-white font-bold py-3 px-6 rounded-lg text-xl transition duration-200'>
                    Play Again
                </a>
            )}
        </div>
        <div className="text-center mt-8 text-gray-500">Game ID: {game.gameId}</div>
      </div>
      {renderJoinRequestDialog()}
      {renderSurrenderDialog()}
    </div>
  );
};

export default Game;