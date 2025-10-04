package com.example.tictactoe;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.model.*;
import com.example.tictactoe.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private GameController gameController;

    private Player player1;
    private Player player2;
    private Game mockGame;

    @BeforeEach
    void setUp() {
        player1 = new Player("Player1");
        player2 = new Player("Player2");

        mockGame = new Game();
        mockGame.setGameId("test-game-id");
        mockGame.setPlayer1(player1);
        mockGame.setPlayer2(player2);
        mockGame.setStatus(GameStatus.IN_PROGRESS);
        mockGame.setCurrentPlayerLogin(player1.getLogin());
    }

    @Test
    void testStartGame() {
        when(gameService.createGame(player1)).thenReturn(mockGame);

        gameController.startGame(player1);

        verify(gameService, times(1)).createGame(player1);
        verify(simpMessagingTemplate, times(1))
                .convertAndSend("/topic/game.created/" + player1.getLogin(), mockGame);
    }

    @Test
    void testConnectToGame_WithGameId() throws InvalidParamException, InvalidGameException {
        ConnectRequest request = new ConnectRequest();
        request.setPlayer(player2);
        request.setGameId("test-game-id");

        Game pendingGame = new Game();
        pendingGame.setGameId("test-game-id");
        pendingGame.setPlayer1(player1);
        pendingGame.setPendingJoinPlayer(player2);
        pendingGame.setStatus(GameStatus.NEW);

        when(gameService.connectToGame(player2, "test-game-id")).thenReturn(pendingGame);

        gameController.connectToGame(request);

        verify(gameService, times(1)).connectToGame(player2, "test-game-id");
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.join.pending/" + player2.getLogin()), any(Game.class));
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.join.request/" + player1.getLogin()), any(Game.class));
    }

    @Test
    void testConnectToGame_WithoutGameId() throws InvalidParamException, InvalidGameException {
        ConnectRequest request = new ConnectRequest();
        request.setPlayer(player2);
        request.setGameId(null);

        Game pendingGame = new Game();
        pendingGame.setGameId("test-game-id");
        pendingGame.setPlayer1(player1);
        pendingGame.setPendingJoinPlayer(player2);
        pendingGame.setStatus(GameStatus.NEW);

        when(gameService.connectToRandomGame(player2)).thenReturn(pendingGame);

        gameController.connectToGame(request);

        verify(gameService, times(1)).connectToRandomGame(player2);
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.join.pending/" + player2.getLogin()), any(Game.class));
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.join.request/" + player1.getLogin()), any(Game.class));
    }

    @Test
    void testConnectToGame_EmptyGameId() throws InvalidParamException, InvalidGameException {
        ConnectRequest request = new ConnectRequest();
        request.setPlayer(player2);
        request.setGameId("");

        Game pendingGame = new Game();
        pendingGame.setGameId("test-game-id");
        pendingGame.setPlayer1(player1);
        pendingGame.setPendingJoinPlayer(player2);
        pendingGame.setStatus(GameStatus.NEW);

        when(gameService.connectToRandomGame(player2)).thenReturn(pendingGame);

        gameController.connectToGame(request);

        verify(gameService, times(1)).connectToRandomGame(player2);
    }

    @Test
    void testConnectToGame_WithException() throws InvalidParamException, InvalidGameException {
        ConnectRequest request = new ConnectRequest();
        request.setPlayer(player2);
        request.setGameId("invalid-game-id");

        when(gameService.connectToGame(player2, "invalid-game-id"))
                .thenThrow(new InvalidParamException("Game not found"));

        assertThrows(InvalidParamException.class, () -> {
            gameController.connectToGame(request);
        });

        verify(gameService, times(1)).connectToGame(player2, "invalid-game-id");
        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void testGamePlay() throws InvalidParamException, InvalidGameException {
        Move move = new Move();
        move.setPlayerLogin(player1.getLogin());
        move.setSquareIndex(0);
        move.setGameId("test-game-id");

        when(gameService.gameplay(move, "test-game-id")).thenReturn(mockGame);

        gameController.gamePlay(move);

        verify(gameService, times(1)).gameplay(move, "test-game-id");
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.test-game-id"), any(Game.class));
    }

    @Test
    void testGamePlay_WithException() throws InvalidParamException, InvalidGameException {
        Move move = new Move();
        move.setPlayerLogin(player1.getLogin());
        move.setSquareIndex(0);
        move.setGameId("test-game-id");

        when(gameService.gameplay(move, "test-game-id"))
                .thenThrow(new InvalidGameException("Not your turn"));

        assertThrows(InvalidGameException.class, () -> {
            gameController.gamePlay(move);
        });

        verify(gameService, times(1)).gameplay(move, "test-game-id");
        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void testSurrender() throws InvalidParamException, InvalidGameException {
        SurrenderRequest request = new SurrenderRequest();
        request.setPlayerLogin(player1.getLogin());
        request.setGameId("test-game-id");

        when(gameService.requestSurrender("test-game-id", player1.getLogin())).thenReturn(mockGame);

        gameController.surrender(request);

        verify(gameService, times(1)).requestSurrender("test-game-id", player1.getLogin());
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.test-game-id"), any(Game.class));
    }

    @Test
    void testSurrender_WithException() throws InvalidParamException, InvalidGameException {
        SurrenderRequest request = new SurrenderRequest();
        request.setPlayerLogin(player1.getLogin());
        request.setGameId("test-game-id");

        when(gameService.requestSurrender("test-game-id", player1.getLogin()))
                .thenThrow(new InvalidGameException("Game not in progress"));

        assertThrows(InvalidGameException.class, () -> {
            gameController.surrender(request);
        });

        verify(gameService, times(1)).requestSurrender("test-game-id", player1.getLogin());
        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void testSurrenderResponse_Accepted() throws InvalidParamException, InvalidGameException {
        SurrenderResponse response = new SurrenderResponse();
        response.setPlayerLogin(player2.getLogin());
        response.setGameId("test-game-id");
        response.setAccepted(true);

        when(gameService.respondToSurrender("test-game-id", player2.getLogin(), true))
                .thenReturn(mockGame);

        gameController.surrenderResponse(response);

        verify(gameService, times(1))
                .respondToSurrender("test-game-id", player2.getLogin(), true);
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.test-game-id"), any(Game.class));
    }

    @Test
    void testSurrenderResponse_Declined() throws InvalidParamException, InvalidGameException {
        SurrenderResponse response = new SurrenderResponse();
        response.setPlayerLogin(player2.getLogin());
        response.setGameId("test-game-id");
        response.setAccepted(false);

        when(gameService.respondToSurrender("test-game-id", player2.getLogin(), false))
                .thenReturn(mockGame);

        gameController.surrenderResponse(response);

        verify(gameService, times(1))
                .respondToSurrender("test-game-id", player2.getLogin(), false);
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/game.test-game-id"), any(Game.class));
    }

    @Test
    void testSurrenderResponse_WithException() throws InvalidParamException, InvalidGameException {
        SurrenderResponse response = new SurrenderResponse();
        response.setPlayerLogin(player2.getLogin());
        response.setGameId("test-game-id");
        response.setAccepted(true);

        when(gameService.respondToSurrender("test-game-id", player2.getLogin(), true))
                .thenThrow(new InvalidGameException("No surrender request"));

        assertThrows(InvalidGameException.class, () -> {
            gameController.surrenderResponse(response);
        });

        verify(gameService, times(1))
                .respondToSurrender("test-game-id", player2.getLogin(), true);
        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}

