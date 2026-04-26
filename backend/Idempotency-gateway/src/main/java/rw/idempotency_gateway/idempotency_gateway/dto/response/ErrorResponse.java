package rw.idempotency_gateway.idempotency_gateway.dto.response;

import java.time.Instant;

public record ErrorResponse(
        String error,
        String message,
        Instant timestamp) {
}
