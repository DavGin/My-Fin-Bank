package com.myfinbank.exception;

import com.myfinbank.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Gestione delle eccezioni Runtime
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Errore: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Errore Interno del Server",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Gestione di una custom exception o errori previsti
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Errore di validazione: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Richiesta Non Valida",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Eccezione generica per evitare errori non gestiti
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        logger.error("Errore inaspettato: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Errore Non Previsto",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Gestione del token mancante
    @ExceptionHandler(MissingTokenException.class)
    public ResponseEntity<ErrorResponse> handleMissingTokenException(MissingTokenException ex, WebRequest request) {
        logger.warn("Token non fornito: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token mancante",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // Gestione del token non valido
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex, WebRequest request) {
        logger.warn("Token non valido: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token non valido",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }


}
