package com.example.tictactoe.storage;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GameStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStorage {

    private static final Logger log = LoggerFactory.getLogger(GameStorage.class);
    private final Map<String, GameEntry> games = new ConcurrentHashMap<>();

    public Map<String, Game> getGames() {
        Map<String, Game> result = new ConcurrentHashMap<>();
        games.forEach((key, entry) -> result.put(key, entry.getGame()));
        return result;
    }

    public void setGame(Game game) {
        games.put(game.getGameId(), new GameEntry(game));
        log.debug("Game {} stored. Total active games: {}", game.getGameId(), games.size());
    }

    public Game getGame(String gameId) {
        GameEntry entry = games.get(gameId);
        if (entry != null) {
            entry.updateLastAccessed();
            return entry.getGame();
        }
        return null;
    }

    public void removeGame(String gameId) {
        games.remove(gameId);
        log.info("Game {} removed from storage", gameId);
    }

    /**
     * Cleanup old games every 5 minutes
     * Removes finished games older than 10 minutes and abandoned games older than 1 hour
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupOldGames() {
        LocalDateTime now = LocalDateTime.now();
        int removedCount = 0;

        for (Map.Entry<String, GameEntry> entry : games.entrySet()) {
            GameEntry gameEntry = entry.getValue();
            Game game = gameEntry.getGame();
            LocalDateTime lastAccessed = gameEntry.getLastAccessed();

            // Remove finished games older than 10 minutes
            if (game.getStatus() == GameStatus.FINISHED && 
                lastAccessed.plusMinutes(10).isBefore(now)) {
                games.remove(entry.getKey());
                removedCount++;
                continue;
            }

            // Remove abandoned games (NEW status, older than 1 hour)
            if (game.getStatus() == GameStatus.NEW && 
                lastAccessed.plusHours(1).isBefore(now)) {
                games.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("Cleaned up {} old games. Remaining games: {}", removedCount, games.size());
        }
    }

    private static class GameEntry {
        private final Game game;
        private LocalDateTime lastAccessed;

        public GameEntry(Game game) {
            this.game = game;
            this.lastAccessed = LocalDateTime.now();
        }

        public Game getGame() {
            return game;
        }

        public LocalDateTime getLastAccessed() {
            return lastAccessed;
        }

        public void updateLastAccessed() {
            this.lastAccessed = LocalDateTime.now();
        }
    }
}
