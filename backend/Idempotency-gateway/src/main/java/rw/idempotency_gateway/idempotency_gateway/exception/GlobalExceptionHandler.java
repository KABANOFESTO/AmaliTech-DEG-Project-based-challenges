package rw.idempotency_gateway.idempotency_gateway.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import rw.idempotency_gateway.idempotency_gateway.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleMissingIdempotencyKey(MissingIdempotencyKeyException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyConflictException exception) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingFailure(PaymentProcessingException exception) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailure(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        if ("Idempotency-Key".equalsIgnoreCase(exception.getHeaderName())) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required.");
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.getReasonPhrase(),
                message,
                Instant.now());

        return ResponseEntity.status(status).body(errorResponse);
    }
}
