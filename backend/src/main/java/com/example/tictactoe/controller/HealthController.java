package com.example.tictactoe.controller;

import com.example.tictactoe.storage.GameStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final GameStorage gameStorage;

    public HealthController(GameStorage gameStorage) {
        this.gameStorage = gameStorage;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "tictactoe");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", gameStorage.getGameCount());
        stats.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(stats);
    }
}

