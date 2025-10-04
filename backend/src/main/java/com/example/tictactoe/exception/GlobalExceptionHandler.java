package com.example.tictactoe.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @MessageExceptionHandler(InvalidParamException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleInvalidParamException(InvalidParamException ex) {
        log.error("Invalid parameter: {}", ex.getMessage());
        return new ErrorMessage("INVALID_PARAMETER", ex.getMessage());
    }

    @MessageExceptionHandler(InvalidGameException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleInvalidGameException(InvalidGameException ex) {
        log.error("Invalid game operation: {}", ex.getMessage());
        return new ErrorMessage("INVALID_GAME_OPERATION", ex.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return new ErrorMessage("INTERNAL_ERROR", "An unexpected error occurred. Please try again.");
    }
}

