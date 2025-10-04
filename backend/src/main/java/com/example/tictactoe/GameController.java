package com.example.tictactoe;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.model.ConnectRequest;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.JoinResponse;
import com.example.tictactoe.model.Move;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.model.RematchRequest;
import com.example.tictactoe.model.RematchResponse;
import com.example.tictactoe.model.SurrenderRequest;
import com.example.tictactoe.model.SurrenderResponse;
import com.example.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@RestController
@Validated
@CrossOrigin(origins = "*")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/game.start")
    public void startGame(@Valid Player player) {
        log.info("start game request: {}", player.getLogin());
        Game game = gameService.createGame(player);
        simpMessagingTemplate.convertAndSend("/topic/game.created/" + player.getLogin(), game);
    }

    @MessageMapping("/game.connect")
    public void connectToGame(@Valid ConnectRequest request) throws InvalidParamException, InvalidGameException {
        log.info("connect request: {} to game {}", request.getPlayer().getLogin(), request.getGameId());
        Game game;
        if (request.getGameId() == null || request.getGameId().isEmpty()) {
            game = gameService.connectToRandomGame(request.getPlayer());
        } else {
            game = gameService.connectToGame(request.getPlayer(), request.getGameId());
        }
        
        // If a new game was created for this player (random game with no available games)
        if (game.getPlayer1().getLogin().equals(request.getPlayer().getLogin()) && game.getPendingJoinPlayer() == null) {
            simpMessagingTemplate.convertAndSend("/topic/game.created/" + request.getPlayer().getLogin(), game);
        } else if (game.getPendingJoinPlayer() != null) {
            // Notify the joining player that request is pending
            simpMessagingTemplate.convertAndSend("/topic/game.join.pending/" + request.getPlayer().getLogin(), game);
            // Notify the game creator about the join request
            simpMessagingTemplate.convertAndSend("/topic/game.join.request/" + game.getPlayer1().getLogin(), game);
        }
    }

    @MessageMapping("/game.join.response")
    public void respondToJoinRequest(@Valid JoinResponse response) throws InvalidParamException, InvalidGameException {
        log.info("join response from: {} in game {} for requester {} -> {}", 
            response.getResponderLogin(), response.getGameId(), response.getRequesterLogin(), response.getAccepted());
        Game game = gameService.respondToJoinRequest(response.getGameId(), response.getResponderLogin(), 
            response.getRequesterLogin(), response.getAccepted());
        
        if (response.getAccepted()) {
            // Notify both players that the game has started
            simpMessagingTemplate.convertAndSend("/topic/game.connected/" + game.getPlayer1().getLogin(), game);
            simpMessagingTemplate.convertAndSend("/topic/game.connected/" + game.getPlayer2().getLogin(), game);
        } else {
            // Notify the requester that their join request was rejected
            simpMessagingTemplate.convertAndSend("/topic/game.join.rejected/" + response.getRequesterLogin(), game);
            // Notify the responder that the request was handled
            simpMessagingTemplate.convertAndSend("/topic/game.updated/" + response.getResponderLogin(), game);
        }
    }

    @MessageMapping("/game.gameplay")
    public void gamePlay(@Valid Move move) throws InvalidParamException, InvalidGameException {
        log.info("gameplay move: {} in game {}", move.getPlayerLogin(), move.getGameId());
        Game game = gameService.gameplay(move, move.getGameId());
        // Update the game status for both players
        simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
    }

    @MessageMapping("/game.surrender")
    public void surrender(@Valid SurrenderRequest request) throws InvalidParamException, InvalidGameException {
        log.info("surrender request from: {} in game {}", request.getPlayerLogin(), request.getGameId());
        Game game = gameService.requestSurrender(request.getGameId(), request.getPlayerLogin());
        simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
    }

    @MessageMapping("/game.surrender.response")
    public void surrenderResponse(@Valid SurrenderResponse response) throws InvalidParamException, InvalidGameException {
        log.info("surrender response from: {} in game {} -> {}", response.getPlayerLogin(), response.getGameId(), response.isAccepted());
        Game game = gameService.respondToSurrender(response.getGameId(), response.getPlayerLogin(), response.isAccepted());
        simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
    }

    @MessageMapping("/game.rematch")
    public void rematch(@Valid RematchRequest request) throws InvalidParamException, InvalidGameException {
        log.info("rematch request from: {} in game {}", request.getPlayerLogin(), request.getGameId());
        Game game = gameService.requestRematch(request.getGameId(), request.getPlayerLogin());
        simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
    }

    @MessageMapping("/game.rematch.response")
    public void rematchResponse(@Valid RematchResponse response) throws InvalidParamException, InvalidGameException {
        log.info("rematch response from: {} in game {} -> {}", response.getPlayerLogin(), response.getGameId(), response.isAccepted());
        Game game = gameService.respondToRematch(response.getGameId(), response.getPlayerLogin(), response.isAccepted());
        simpMessagingTemplate.convertAndSend("/topic/game." + game.getGameId(), game);
    }

    @GetMapping("/api/games/available")
    public java.util.List<Game> getAvailableGames() {
        log.info("get available games request");
        return gameService.getAvailableGames();
    }
}