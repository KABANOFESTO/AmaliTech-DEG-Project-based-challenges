package rw.idempotency_gateway.idempotency_gateway.service;

import rw.idempotency_gateway.idempotency_gateway.model.StoredResponse;

public record PaymentProcessingResult(
        StoredResponse storedResponse,
        boolean cacheHit) {
}
