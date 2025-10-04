package com.example.tictactoe.storage;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    public GameStorage(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Game> getGames() {
        Map<String, Game> result = new HashMap<>();
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        if (keys != null) {
            for (String key : keys) {
                Game game = (Game) redisTemplate.opsForValue().get(key);
                if (game != null) {
                    result.put(game.getGameId(), game);
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
        return (Game) redisTemplate.opsForValue().get(key);
    }

    public void removeGame(String gameId) {
        String key = keyPrefix + gameId;
        redisTemplate.delete(key);
        log.info("Game {} removed from Redis", gameId);
    }

    /**
     * Cleanup old games every 30 minutes
     * Removes finished games older than 10 minutes (Redis TTL handles most cleanup)
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void cleanupOldGames() {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int removedCount = 0;
        for (String key : keys) {
            Game game = (Game) redisTemplate.opsForValue().get(key);
            if (game != null && game.getStatus() == GameStatus.FINISHED) {
                // Shorten TTL for finished games to 10 minutes
                redisTemplate.expire(key, 10, TimeUnit.MINUTES);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("Updated TTL for {} finished games. Total games in Redis: {}", 
                removedCount, keys.size());
        }
    }

    /**
     * Get count of active games
     */
    public long getGameCount() {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        return keys != null ? keys.size() : 0;
    }
}
