package com.example.tictactoe.storage;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import com.example.tictactoe.model.Player;
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
class GameStorageTest {

    private GameStorage gameStorage;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
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
        
        // Clear the game storage before each test
        inMemoryGames.clear();
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

