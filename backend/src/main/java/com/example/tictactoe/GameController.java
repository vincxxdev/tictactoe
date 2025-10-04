package com.example.tictactoe;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.model.ConnectRequest;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.Move;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.model.SurrenderRequest;
import com.example.tictactoe.model.SurrenderResponse;
import com.example.tictactoe.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/game.start")
    public void startGame(Player player) {
        log.info("start game request: {}", player.getLogin());
        Game game = gameService.createGame(player);
        simpMessagingTemplate.convertAndSend("/topic/game.created/" + player.getLogin(), game);
    }

    @MessageMapping("/game.connect")
    public void connectToGame(ConnectRequest request) {
        log.info("connect request: {} to game {}", request.getPlayer().getLogin(), request.getGameId());
        try {
            Game game;
            if (request.getGameId() == null || request.getGameId().isEmpty()) {
                game = gameService.connectToRandomGame(request.getPlayer());
            } else {
                game = gameService.connectToGame(request.getPlayer(), request.getGameId());
            }
            // Notify the player that they have connected to the game
            simpMessagingTemplate.convertAndSend("/topic/game.connected/" + request.getPlayer().getLogin(), game);
            // Notify the other player that someone has connected to the game
            if (game.getPlayer2() != null) {
                 String otherPlayer = request.getPlayer().getLogin().equals(game.getPlayer1().getLogin()) ? game.getPlayer2().getLogin() : game.getPlayer1().getLogin();
                 simpMessagingTemplate.convertAndSend("/topic/game.connected/" + otherPlayer, game);
            }
        } catch (InvalidParamException | InvalidGameException e) {
            log.error("Error connecting to game: {}", e.getMessage());
        }
    }

    @MessageMapping("/game.gameplay")
    public void gamePlay(Move move) {
        log.info("gameplay move: {} in game {}", move.getPlayerLogin(), move.getGameId());
        try {
            Game game = gameService.gameplay(move, move.getGameId());
            // Update the game status for both players
            simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
        } catch (InvalidParamException | InvalidGameException e) {
            log.error("Error during gameplay: {}", e.getMessage());
        }
    }

    @MessageMapping("/game.surrender")
    public void surrender(SurrenderRequest request) {
        log.info("surrender request from: {} in game {}", request.getPlayerLogin(), request.getGameId());
        try {
            Game game = gameService.requestSurrender(request.getGameId(), request.getPlayerLogin());
            simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
        } catch (InvalidParamException | InvalidGameException e) {
            log.error("Error during surrender request: {}", e.getMessage());
        }
    }

    @MessageMapping("/game.surrender.response")
    public void surrenderResponse(SurrenderResponse response) {
        log.info("surrender response from: {} in game {} -> {}", response.getPlayerLogin(), response.getGameId(), response.isAccepted());
        try {
            Game game = gameService.respondToSurrender(response.getGameId(), response.getPlayerLogin(), response.isAccepted());
            simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
        } catch (InvalidParamException | InvalidGameException e) {
            log.error("Error during surrender response: {}", e.getMessage());
        }
    }
}