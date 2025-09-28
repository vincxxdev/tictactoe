export interface Player {
  id: string;
  name: string;
  symbol: 'X' | 'O';
}

export interface GameState {
  board: (string | null)[];
  currentPlayer: Player;
  winner: Player | null;
  isDraw: boolean;
}
