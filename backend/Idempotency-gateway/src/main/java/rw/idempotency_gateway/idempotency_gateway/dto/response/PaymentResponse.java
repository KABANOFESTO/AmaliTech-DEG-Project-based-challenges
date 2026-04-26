package rw.idempotency_gateway.idempotency_gateway.dto.response;

import java.time.Instant;

public record PaymentResponse(
        String message,
        String idempotencyKey,
        Instant processedAt) {
}
