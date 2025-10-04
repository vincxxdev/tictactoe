package com.example.tictactoe.storage;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import com.example.tictactoe.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStorageTest {

    private GameStorage gameStorage;

    @BeforeEach
    void setUp() {
        gameStorage = GameStorage.getInstance();
        gameStorage.getGames().clear();
    }

    @Test
    void testGetInstance_ReturnsSingleton() {
        GameStorage instance1 = GameStorage.getInstance();
        GameStorage instance2 = GameStorage.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    void testSetGame_AddsGameToStorage() {
        Game game = new Game();
        game.setGameId("test-game-id");
        game.setPlayer1(new Player("Player1"));
        game.setStatus(GameStatus.NEW);

        gameStorage.setGame(game);

        assertTrue(gameStorage.getGames().containsKey("test-game-id"));
        assertEquals(game, gameStorage.getGames().get("test-game-id"));
    }

    @Test
    void testSetGame_UpdatesExistingGame() {
        Game game = new Game();
        game.setGameId("test-game-id");
        game.setPlayer1(new Player("Player1"));
        game.setStatus(GameStatus.NEW);

        gameStorage.setGame(game);

        // Update the game
        game.setPlayer2(new Player("Player2"));
        game.setStatus(GameStatus.IN_PROGRESS);
        gameStorage.setGame(game);

        Game retrievedGame = gameStorage.getGames().get("test-game-id");
        assertEquals(GameStatus.IN_PROGRESS, retrievedGame.getStatus());
        assertNotNull(retrievedGame.getPlayer2());
        assertEquals("Player2", retrievedGame.getPlayer2().getLogin());
    }

    @Test
    void testGetGames_ReturnsAllGames() {
        Game game1 = new Game();
        game1.setGameId("game-1");
        game1.setPlayer1(new Player("Player1"));

        Game game2 = new Game();
        game2.setGameId("game-2");
        game2.setPlayer1(new Player("Player2"));

        gameStorage.setGame(game1);
        gameStorage.setGame(game2);

        assertEquals(2, gameStorage.getGames().size());
        assertTrue(gameStorage.getGames().containsKey("game-1"));
        assertTrue(gameStorage.getGames().containsKey("game-2"));
    }

    @Test
    void testGetGames_EmptyStorage() {
        assertTrue(gameStorage.getGames().isEmpty());
    }
}

