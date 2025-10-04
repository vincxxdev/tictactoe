package com.example.tictactoe.service;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.model.TicToe;
import com.example.tictactoe.storage.GameStorage;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameService {

    public Game createGame(Player player) {
        Game game = new Game();
        game.setBoard(new String[9]);
        game.setGameId(UUID.randomUUID().toString());
        game.setPlayer1(player);
        game.setStatus(GameStatus.NEW);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game connectToGame(Player player2, String gameId) throws InvalidParamException, InvalidGameException {
        if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new InvalidParamException("Game with provided ID does not exist");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);
        if (game.getPlayer2() != null) {
            throw new InvalidGameException("Game is already full");
        }
        game.setPlayer2(player2);
        game.setCurrentPlayerLogin(game.getPlayer1().getLogin());
        game.setStatus(GameStatus.IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game connectToRandomGame(Player player2) throws InvalidGameException {
        Game game = GameStorage.getInstance().getGames().values().stream()
                .filter(it -> it.getStatus().equals(GameStatus.NEW))
                .findFirst().orElse(null);

        if (game == null) {
            return createGame(player2);
        }
        
        game.setPlayer2(player2);
        game.setCurrentPlayerLogin(game.getPlayer1().getLogin());
        game.setStatus(GameStatus.IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        return game;
    }
    public Game gameplay(com.example.tictactoe.model.Move move, String gameId) throws InvalidParamException, InvalidGameException {
        if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new InvalidParamException("Game with provided ID does not exist");
        }

        Game game = GameStorage.getInstance().getGames().get(gameId);
        if (game.getStatus().equals(GameStatus.FINISHED)) {
            throw new InvalidGameException("Game is already finished");
        }

        if (!game.getCurrentPlayerLogin().equals(move.getPlayerLogin())) {
            throw new InvalidGameException("It's not your turn");
        }

        String[] board = game.getBoard();
        if (board[move.getSquareIndex()] != null) {
            throw new InvalidGameException("Square is not empty");
        }

        TicToe playerSymbol = game.getPlayer1().getLogin().equals(move.getPlayerLogin()) ? TicToe.X : TicToe.O;
        board[move.getSquareIndex()] = playerSymbol.toString();

        if (checkWinner(board, playerSymbol)) {
            game.setWinner(playerSymbol);
            game.setStatus(GameStatus.FINISHED);
        } else if (isBoardFull(board)) {
            game.setStatus(GameStatus.FINISHED);
        }

        // Switch player
        if (game.getStatus() != GameStatus.FINISHED) {
            String nextPlayerLogin = game.getPlayer1().getLogin().equals(move.getPlayerLogin()) ? game.getPlayer2().getLogin() : game.getPlayer1().getLogin();
            game.setCurrentPlayerLogin(nextPlayerLogin);
        }

        GameStorage.getInstance().setGame(game);
        return game;
    }

    private boolean isBoardFull(String[] board) {
        for (String s : board) {
            if (s == null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkWinner(String[] board, TicToe ticToe) {
        // Winning combination
        int[][] winCombinations = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
                {0, 4, 8}, {2, 4, 6}  // Diagonals
        };

        for (int[] combination : winCombinations) {
            if (board[combination[0]] == ticToe.toString() &&
                board[combination[1]] == ticToe.toString() &&
                board[combination[2]] == ticToe.toString()) {
                return true;
            }
        }
        return false;
    }

    public Game requestSurrender(String gameId, String playerLogin) throws InvalidParamException, InvalidGameException {
        Game game = getGameById(gameId);
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidGameException("Game is not in progress");
        }
        game.setSurrenderRequesterLogin(playerLogin);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game respondToSurrender(String gameId, String responderLogin, boolean accepted) throws InvalidParamException, InvalidGameException {
        Game game = getGameById(gameId);
        if (game.getSurrenderRequesterLogin() == null || game.getSurrenderRequesterLogin().equals(responderLogin)) {
            throw new InvalidGameException("No surrender request to respond to");
        }

        if (accepted) {
            game.setStatus(GameStatus.FINISHED);
            TicToe winnerSymbol = game.getPlayer1().getLogin().equals(responderLogin) ? TicToe.X : TicToe.O;
            game.setWinner(winnerSymbol);
        }

        // Reset surrender request after response
        game.setSurrenderRequesterLogin(null);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    private Game getGameById(String gameId) throws InvalidParamException {
        if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new InvalidParamException("Game with provided ID does not exist");
        }
        return GameStorage.getInstance().getGames().get(gameId);
    }
}
