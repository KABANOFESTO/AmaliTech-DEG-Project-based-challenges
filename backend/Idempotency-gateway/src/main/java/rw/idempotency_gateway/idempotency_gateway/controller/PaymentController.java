package rw.idempotency_gateway.idempotency_gateway.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import rw.idempotency_gateway.idempotency_gateway.dto.request.PaymentRequest;
import rw.idempotency_gateway.idempotency_gateway.dto.response.PaymentResponse;
import rw.idempotency_gateway.idempotency_gateway.model.StoredResponse;
import rw.idempotency_gateway.idempotency_gateway.service.PaymentProcessingResult;
import rw.idempotency_gateway.idempotency_gateway.service.PaymentService;

@RestController
public class PaymentController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String CACHE_HIT_HEADER = "X-Cache-Hit";

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process-payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody PaymentRequest paymentRequest) {

        PaymentProcessingResult result = paymentService.processPayment(idempotencyKey, paymentRequest);
        StoredResponse storedResponse = result.storedResponse();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(storedResponse.statusCode());

        if (result.cacheHit()) {
            responseBuilder.header(CACHE_HIT_HEADER, Boolean.TRUE.toString());
        }

        return responseBuilder.body(storedResponse.body());
    }
}
