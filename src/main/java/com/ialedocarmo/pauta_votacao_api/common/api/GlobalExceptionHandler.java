package com.ialedocarmo.pauta_votacao_api.common.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        int statusCode = ex.getStatusCode().value();
        ApiError body = buildError(statusCode, safeMessage(ex.getReason()), request.getRequestURI());
        return ResponseEntity.status(statusCode).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        ApiError body = buildError(HttpStatus.BAD_REQUEST.value(), safeMessage(message), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ApiError body = buildError(HttpStatus.BAD_REQUEST.value(), "corpo da requisicao invalido", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error while processing request: {}", request.getRequestURI(), ex);
        ApiError body = buildError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "erro interno inesperado", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiError buildError(int status, String message, String path) {
        return new ApiError(OffsetDateTime.now(clock), status, message, path);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + safeMessage(fieldError.getDefaultMessage());
    }

    private String safeMessage(String message) {
        return (message == null || message.isBlank()) ? "erro de validacao" : message;
    }
}
