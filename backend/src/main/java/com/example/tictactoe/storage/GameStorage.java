package com.example.tictactoe.storage;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class GameStorage {

    private static final Logger log = LoggerFactory.getLogger(GameStorage.class);
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${game.redis.key-prefix}")
    private String keyPrefix;
    
    @Value("${game.redis.ttl-hours}")
    private long ttlHours;

    @Value("${game.new-game-max-age-minutes:10}")
    private int newGameMaxAgeMinutes;

    public GameStorage(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Game> getGames() {
        Map<String, Game> result = new HashMap<>();
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        if (keys != null) {
            for (String key : keys) {
                try {
                    Object obj = redisTemplate.opsForValue().get(key);
                    if (obj instanceof Game) {
                        Game game = (Game) obj;
                        result.put(game.getGameId(), game);
                    } else if (obj != null) {
                        // Handle old data without type info - skip it or delete it
                        log.warn("Found game data without type information in key {}, deleting it", key);
                        redisTemplate.delete(key);
                    }
                } catch (Exception e) {
                    log.error("Error deserializing game from key {}: {}", key, e.getMessage());
                    // Optionally delete corrupted data
                    redisTemplate.delete(key);
                }
            }
        }
        return result;
    }

    public void setGame(Game game) {
        String key = keyPrefix + game.getGameId();
        redisTemplate.opsForValue().set(key, game, ttlHours, TimeUnit.HOURS);
        log.debug("Game {} stored in Redis with TTL of {} hours", game.getGameId(), ttlHours);
    }

    public Game getGame(String gameId) {
        String key = keyPrefix + gameId;
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj instanceof Game) {
                return (Game) obj;
            } else if (obj != null) {
                log.warn("Found game data without type information for gameId {}, deleting it", gameId);
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("Error deserializing game {}: {}", gameId, e.getMessage());
            redisTemplate.delete(key);
        }
        return null;
    }

    public void removeGame(String gameId) {
        String key = keyPrefix + gameId;
        redisTemplate.delete(key);
        log.info("Game {} removed from Redis", gameId);
    }

    /**
     * Cleanup old games every 30 minutes
     * Removes finished games older than 10 minutes (Redis TTL handles most cleanup)
     * Also removes NEW games that are too old (abandoned lobbies)
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void cleanupOldGames() {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int finishedCount = 0;
        int abandonedCount = 0;
        
        for (String key : keys) {
            Game game = (Game) redisTemplate.opsForValue().get(key);
            if (game != null) {
                if (game.getStatus() == GameStatus.FINISHED) {
                    // Shorten TTL for finished games to 10 minutes
                    redisTemplate.expire(key, 10, TimeUnit.MINUTES);
                    finishedCount++;
                } else if (game.getStatus() == GameStatus.NEW && isGameAbandoned(game)) {
                    // Remove abandoned NEW games (too old)
                    redisTemplate.delete(key);
                    abandonedCount++;
                    log.info("Removed abandoned game {} created by {}", 
                        game.getGameId(), game.getPlayer1().getLogin());
                }
            }
        }

        if (finishedCount > 0 || abandonedCount > 0) {
            log.info("Cleanup: Updated TTL for {} finished games, removed {} abandoned games. Total games: {}", 
                finishedCount, abandonedCount, keys.size() - abandonedCount);
        }
    }

    private boolean isGameAbandoned(Game game) {
        if (game.getCreatedAt() == null) {
            return false;
        }
        Duration age = Duration.between(game.getCreatedAt(), Instant.now());
        return age.toMinutes() > newGameMaxAgeMinutes;
    }

    /**
     * Get count of active games
     */
    public long getGameCount() {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        return keys != null ? keys.size() : 0;
    }
}
