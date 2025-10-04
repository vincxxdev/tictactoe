package com.example.tictactoe.service;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import com.example.tictactoe.model.Move;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.model.TicToe;
import com.example.tictactoe.storage.GameStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private GameService gameService;
    private GameStorage gameStorage;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    private Player player1;
    private Player player2;
    private Map<String, Game> inMemoryGames;

    @BeforeEach
    void setUp() {
        // Setup mock Redis
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // In-memory storage for testing
        inMemoryGames = new HashMap<>();
        
        // Mock Redis operations to use in-memory storage
        lenient().doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Game game = invocation.getArgument(1);
            inMemoryGames.put(key, game);
            return null;
        }).when(valueOperations).set(anyString(), any(Game.class), anyLong(), any());
        
        lenient().when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return inMemoryGames.get(key);
        });
        
        lenient().when(redisTemplate.keys(anyString())).thenAnswer(invocation -> {
            Set<String> keys = new HashSet<>(inMemoryGames.keySet());
            return keys;
        });
        
        lenient().doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            inMemoryGames.remove(key);
            return true;
        }).when(redisTemplate).delete(anyString());
        
        // Create GameStorage with mocked Redis
        gameStorage = new GameStorage(redisTemplate);
        ReflectionTestUtils.setField(gameStorage, "keyPrefix", "tictactoe:game:");
        ReflectionTestUtils.setField(gameStorage, "ttlHours", 24L);
        
        gameService = new GameService(gameStorage);
        player1 = new Player("Player1");
        player2 = new Player("Player2");
        
        // Clear the game storage before each test
        inMemoryGames.clear();
    }

    @Test
    void testCreateGame() {
        Game game = gameService.createGame(player1);

        assertNotNull(game);
        assertNotNull(game.getGameId());
        assertEquals(player1, game.getPlayer1());
        assertNull(game.getPlayer2());
        assertEquals(GameStatus.NEW, game.getStatus());
        assertNotNull(game.getBoard());
        assertEquals(9, game.getBoard().length);
    }

    @Test
    void testConnectToGame_Success() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        String gameId = createdGame.getGameId();

        Game game = gameService.connectToGame(player2, gameId);

        assertNotNull(game);
        assertEquals(player1, game.getPlayer1());
        assertNull(game.getPlayer2()); // Player2 is not added yet
        assertEquals(player2, game.getPendingJoinPlayer()); // But set as pending
        assertEquals(GameStatus.NEW, game.getStatus()); // Status remains NEW until accepted
    }

    @Test
    void testConnectToGame_GameNotFound() {
        assertThrows(InvalidParamException.class, () -> {
            gameService.connectToGame(player2, "non-existent-game-id");
        });
    }

    @Test
    void testConnectToGame_GameAlreadyFull() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        String gameId = createdGame.getGameId();
        
        // First, connect and accept player2
        gameService.connectToGame(player2, gameId);
        gameService.respondToJoinRequest(gameId, player1.getLogin(), player2.getLogin(), true);

        Player player3 = new Player("Player3");
        assertThrows(InvalidGameException.class, () -> {
            gameService.connectToGame(player3, gameId);
        });
    }

    @Test
    void testConnectToRandomGame_NewGame() throws InvalidGameException {
        Game game = gameService.connectToRandomGame(player1);

        assertNotNull(game);
        assertEquals(player1, game.getPlayer1());
        assertNull(game.getPlayer2());
        assertEquals(GameStatus.NEW, game.getStatus());
    }

    @Test
    void testConnectToRandomGame_JoinsExistingGame() throws InvalidGameException {
        gameService.createGame(player1);

        Game game = gameService.connectToRandomGame(player2);

        assertNotNull(game);
        assertEquals(player1, game.getPlayer1());
        assertNull(game.getPlayer2()); // Player2 is not added yet
        assertEquals(player2, game.getPendingJoinPlayer()); // But set as pending
        assertEquals(GameStatus.NEW, game.getStatus()); // Status remains NEW until accepted
    }

    @Test
    void testGameplay_ValidMove() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        Move move = new Move();
        move.setPlayerLogin(player1.getLogin());
        move.setSquareIndex(0);
        move.setGameId(createdGame.getGameId());

        Game game = gameService.gameplay(move, createdGame.getGameId());

        assertEquals("X", game.getBoard()[0]);
        assertEquals(player2.getLogin(), game.getCurrentPlayerLogin());
    }

    @Test
    void testGameplay_GameNotFound() {
        Move move = new Move();
        move.setPlayerLogin(player1.getLogin());
        move.setSquareIndex(0);
        move.setGameId("non-existent");

        assertThrows(InvalidParamException.class, () -> {
            gameService.gameplay(move, "non-existent");
        });
    }

    @Test
    void testGameplay_NotPlayerTurn() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        Move move = new Move();
        move.setPlayerLogin(player2.getLogin()); // Player 2 tries to move first
        move.setSquareIndex(0);
        move.setGameId(createdGame.getGameId());

        assertThrows(InvalidGameException.class, () -> {
            gameService.gameplay(move, createdGame.getGameId());
        });
    }

    @Test
    void testGameplay_SquareNotEmpty() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        Move move1 = new Move();
        move1.setPlayerLogin(player1.getLogin());
        move1.setSquareIndex(0);
        move1.setGameId(createdGame.getGameId());
        gameService.gameplay(move1, createdGame.getGameId());

        Move move2 = new Move();
        move2.setPlayerLogin(player2.getLogin());
        move2.setSquareIndex(0); // Same square
        move2.setGameId(createdGame.getGameId());

        assertThrows(InvalidGameException.class, () -> {
            gameService.gameplay(move2, createdGame.getGameId());
        });
    }

    @Test
    void testGameplay_WinCondition_HorizontalRow() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        // Player 1: 0, 1, 2 (winning row)
        // Player 2: 3, 4
        playMove(createdGame.getGameId(), player1, 0);
        playMove(createdGame.getGameId(), player2, 3);
        playMove(createdGame.getGameId(), player1, 1);
        playMove(createdGame.getGameId(), player2, 4);
        Game game = playMove(createdGame.getGameId(), player1, 2);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(TicToe.X, game.getWinner());
    }

    @Test
    void testGameplay_WinCondition_VerticalColumn() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        // Player 1: 0, 3, 6 (winning column)
        // Player 2: 1, 2
        playMove(createdGame.getGameId(), player1, 0);
        playMove(createdGame.getGameId(), player2, 1);
        playMove(createdGame.getGameId(), player1, 3);
        playMove(createdGame.getGameId(), player2, 2);
        Game game = playMove(createdGame.getGameId(), player1, 6);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(TicToe.X, game.getWinner());
    }

    @Test
    void testGameplay_WinCondition_Diagonal() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        // Player 1: 0, 4, 8 (winning diagonal)
        // Player 2: 1, 2
        playMove(createdGame.getGameId(), player1, 0);
        playMove(createdGame.getGameId(), player2, 1);
        playMove(createdGame.getGameId(), player1, 4);
        playMove(createdGame.getGameId(), player2, 2);
        Game game = playMove(createdGame.getGameId(), player1, 8);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(TicToe.X, game.getWinner());
    }

    @Test
    void testGameplay_DrawCondition() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        // Create a draw scenario
        // X O X
        // O X X
        // O X O
        playMove(createdGame.getGameId(), player1, 0); // X
        playMove(createdGame.getGameId(), player2, 1); // O
        playMove(createdGame.getGameId(), player1, 2); // X
        playMove(createdGame.getGameId(), player2, 3); // O
        playMove(createdGame.getGameId(), player1, 4); // X
        playMove(createdGame.getGameId(), player2, 6); // O
        playMove(createdGame.getGameId(), player1, 5); // X
        playMove(createdGame.getGameId(), player2, 8); // O
        Game game = playMove(createdGame.getGameId(), player1, 7); // X

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertNull(game.getWinner());
    }

    @Test
    void testRequestSurrender_Success() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        Game game = gameService.requestSurrender(createdGame.getGameId(), player1.getLogin());

        assertEquals(player1.getLogin(), game.getSurrenderRequesterLogin());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
    }

    @Test
    void testRequestSurrender_GameNotInProgress() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);

        assertThrows(InvalidGameException.class, () -> {
            gameService.requestSurrender(createdGame.getGameId(), player1.getLogin());
        });
    }

    @Test
    void testRespondToSurrender_Accepted() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);
        gameService.requestSurrender(createdGame.getGameId(), player1.getLogin());

        Game game = gameService.respondToSurrender(createdGame.getGameId(), player2.getLogin(), true);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(TicToe.O, game.getWinner()); // Player 2 wins
        assertNull(game.getSurrenderRequesterLogin());
    }

    @Test
    void testRespondToSurrender_Declined() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());
        // Accept the join request
        gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);
        gameService.requestSurrender(createdGame.getGameId(), player1.getLogin());

        Game game = gameService.respondToSurrender(createdGame.getGameId(), player2.getLogin(), false);

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNull(game.getWinner());
        assertNull(game.getSurrenderRequesterLogin());
    }

    @Test
    void testRespondToSurrender_NoRequest() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());

        assertThrows(InvalidGameException.class, () -> {
            gameService.respondToSurrender(createdGame.getGameId(), player2.getLogin(), true);
        });
    }

    @Test
    void testRespondToJoinRequest_Accepted() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());

        Game game = gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);

        assertNotNull(game);
        assertEquals(player1, game.getPlayer1());
        assertEquals(player2, game.getPlayer2());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(player1.getLogin(), game.getCurrentPlayerLogin());
        assertNull(game.getPendingJoinPlayer());
    }

    @Test
    void testRespondToJoinRequest_Rejected() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());

        Game game = gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), false);

        assertNotNull(game);
        assertEquals(player1, game.getPlayer1());
        assertNull(game.getPlayer2());
        assertEquals(GameStatus.NEW, game.getStatus());
        assertNull(game.getPendingJoinPlayer());
    }

    @Test
    void testRespondToJoinRequest_NoPendingPlayer() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);

        assertThrows(InvalidGameException.class, () -> {
            gameService.respondToJoinRequest(createdGame.getGameId(), player1.getLogin(), player2.getLogin(), true);
        });
    }

    @Test
    void testRespondToJoinRequest_WrongResponder() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());

        Player player3 = new Player("Player3");
        assertThrows(InvalidGameException.class, () -> {
            gameService.respondToJoinRequest(createdGame.getGameId(), player3.getLogin(), player2.getLogin(), true);
        });
    }

    @Test
    void testConnectToGame_WithPendingJoinPlayer() throws InvalidParamException, InvalidGameException {
        Game createdGame = gameService.createGame(player1);
        gameService.connectToGame(player2, createdGame.getGameId());

        Player player3 = new Player("Player3");
        assertThrows(InvalidGameException.class, () -> {
            gameService.connectToGame(player3, createdGame.getGameId());
        });
    }

    // Helper method to make moves
    private Game playMove(String gameId, Player player, int squareIndex) throws InvalidParamException, InvalidGameException {
        Move move = new Move();
        move.setPlayerLogin(player.getLogin());
        move.setSquareIndex(squareIndex);
        move.setGameId(gameId);
        return gameService.gameplay(move, gameId);
    }
}

