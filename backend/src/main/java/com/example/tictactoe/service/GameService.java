package com.example.tictactoe.service;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.model.TicToe;
import com.example.tictactoe.storage.GameStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class GameService {

    private final GameStorage gameStorage;
    
    @Value("${game.new-game-max-age-minutes:10}")
    private int newGameMaxAgeMinutes;

    public GameService(GameStorage gameStorage) {
        this.gameStorage = gameStorage;
    }

    public Game createGame(Player player) {
        Game game = new Game();
        game.setBoard(new String[9]);
        game.setGameId(UUID.randomUUID().toString());
        game.setPlayer1(player);
        game.setStatus(GameStatus.NEW);
        gameStorage.setGame(game);
        return game;
    }

    public Game connectToGame(Player player2, String gameId) throws InvalidParamException, InvalidGameException {
        if (!gameStorage.getGames().containsKey(gameId)) {
            throw new InvalidParamException("Game with provided ID does not exist");
        }
        Game game = gameStorage.getGames().get(gameId);
        if (game.getPlayer2() != null) {
            throw new InvalidGameException("Game is already full");
        }
        if (game.getPendingJoinPlayer() != null) {
            throw new InvalidGameException("There is already a pending join request");
        }
        // Set pending join player instead of directly adding player2
        game.setPendingJoinPlayer(player2);
        game.updateLastActivity();
        gameStorage.setGame(game);
        return game;
    }

    public Game connectToRandomGame(Player player2) throws InvalidGameException {
        Game game = gameStorage.getGames().values().stream()
                .filter(it -> it.getStatus().equals(GameStatus.NEW))
                .filter(it -> !isGameTooOld(it))
                .filter(it -> it.getPendingJoinPlayer() == null)
                .findFirst().orElse(null);

        if (game == null) {
            return createGame(player2);
        }
        
        // Set pending join player instead of directly adding player2
        game.setPendingJoinPlayer(player2);
        game.updateLastActivity();
        gameStorage.setGame(game);
        return game;
    }
    public Game gameplay(com.example.tictactoe.model.Move move, String gameId) throws InvalidParamException, InvalidGameException {
        if (!gameStorage.getGames().containsKey(gameId)) {
            throw new InvalidParamException("Game with provided ID does not exist");
        }

        Game game = gameStorage.getGames().get(gameId);
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

        game.updateLastActivity();
        gameStorage.setGame(game);
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

        String symbol = ticToe.toString();
        for (int[] combination : winCombinations) {
            if (board[combination[0]] != null && board[combination[0]].equals(symbol) &&
                board[combination[1]] != null && board[combination[1]].equals(symbol) &&
                board[combination[2]] != null && board[combination[2]].equals(symbol)) {
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
        gameStorage.setGame(game);
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
        gameStorage.setGame(game);
        return game;
    }

    public Game respondToJoinRequest(String gameId, String responderLogin, String requesterLogin, boolean accepted) throws InvalidParamException, InvalidGameException {
        Game game = getGameById(gameId);
        
        if (game.getPendingJoinPlayer() == null) {
            throw new InvalidGameException("No pending join request");
        }
        
        if (!game.getPlayer1().getLogin().equals(responderLogin)) {
            throw new InvalidGameException("Only the game creator can respond to join requests");
        }
        
        if (!game.getPendingJoinPlayer().getLogin().equals(requesterLogin)) {
            throw new InvalidGameException("Invalid requester");
        }

        if (accepted) {
            // Accept the join request - add player2 and start the game
            game.setPlayer2(game.getPendingJoinPlayer());
            game.setCurrentPlayerLogin(game.getPlayer1().getLogin());
            game.setStatus(GameStatus.IN_PROGRESS);
        }
        
        // Clear the pending join player whether accepted or rejected
        game.setPendingJoinPlayer(null);
        game.updateLastActivity();
        gameStorage.setGame(game);
        return game;
    }

    private Game getGameById(String gameId) throws InvalidParamException {
        if (!gameStorage.getGames().containsKey(gameId)) {
            throw new InvalidParamException("Game with provided ID does not exist");
        }
        return gameStorage.getGames().get(gameId);
    }

    public java.util.List<Game> getAvailableGames() {
        return gameStorage.getGames().values().stream()
                .filter(game -> game.getStatus().equals(GameStatus.NEW))
                .filter(game -> !isGameTooOld(game))
                .sorted((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isGameTooOld(Game game) {
        if (game.getCreatedAt() == null) {
            return false;
        }
        Duration age = Duration.between(game.getCreatedAt(), Instant.now());
        return age.toMinutes() > newGameMaxAgeMinutes;
    }

    public Game requestRematch(String gameId, String playerLogin) throws InvalidParamException, InvalidGameException {
        Game game = getGameById(gameId);
        if (game.getStatus() != GameStatus.FINISHED) {
            throw new InvalidGameException("Can only request rematch for finished games");
        }
        game.setRematchRequesterLogin(playerLogin);
        gameStorage.setGame(game);
        return game;
    }

    public Game respondToRematch(String gameId, String responderLogin, boolean accepted) throws InvalidParamException, InvalidGameException {
        Game game = getGameById(gameId);
        if (game.getRematchRequesterLogin() == null || game.getRematchRequesterLogin().equals(responderLogin)) {
            throw new InvalidGameException("No rematch request to respond to");
        }

        if (accepted) {
            // Reset the game for a rematch
            game.setBoard(new String[9]);
            game.setStatus(GameStatus.IN_PROGRESS);
            game.setWinner(null);
            game.setCurrentPlayerLogin(game.getPlayer1().getLogin());
            game.setSurrenderRequesterLogin(null);
        }

        // Reset rematch request after response
        game.setRematchRequesterLogin(null);
        game.updateLastActivity();
        gameStorage.setGame(game);
        return game;
    }
}
