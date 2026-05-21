package com.appsdeveloperblog.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Java 17 sealed class + pattern matching for instanceof:
     * Handles all ApplicationException subtypes in a single handler.
     * The sealed hierarchy guarantees only known subtypes exist.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        // Java 16+: Pattern matching for instanceof — no separate cast needed
        if (ex instanceof UserNotFoundException notFound) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(notFound.getErrorCode(),
                            "User [id=%d] not found".formatted(notFound.getUserId())));
        } else if (ex instanceof ResourceConflictException conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(conflict.getErrorCode(),
                            "Conflict on %s.%s: %s".formatted(
                                    conflict.getResourceName(),
                                    conflict.getConflictField(),
                                    conflict.getMessage())));
        }

        // Fallback (sealed class guarantees this is unreachable for known subtypes)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("UNKNOWN_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Java 16+: pattern matching for instanceof using the binding variable
        var firstError = ex.getBindingResult().getAllErrors().get(0);
        var message = firstError.getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }
}
