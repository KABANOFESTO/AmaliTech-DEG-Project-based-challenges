package rw.idempotency_gateway.idempotency_gateway.model;

import rw.idempotency_gateway.idempotency_gateway.dto.response.PaymentResponse;

public record StoredResponse(
        int statusCode,
        PaymentResponse body) {
}
