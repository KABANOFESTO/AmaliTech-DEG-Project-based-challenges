package rw.idempotency_gateway.idempotency_gateway.service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import rw.idempotency_gateway.idempotency_gateway.dto.request.PaymentRequest;
import rw.idempotency_gateway.idempotency_gateway.dto.response.PaymentResponse;
import rw.idempotency_gateway.idempotency_gateway.exception.IdempotencyConflictException;
import rw.idempotency_gateway.idempotency_gateway.exception.MissingIdempotencyKeyException;
import rw.idempotency_gateway.idempotency_gateway.exception.PaymentProcessingException;
import rw.idempotency_gateway.idempotency_gateway.model.IdempotencyRecord;
import rw.idempotency_gateway.idempotency_gateway.model.StoredResponse;

@Service
public class PaymentService {

    private static final int RESPONSE_STATUS = HttpStatus.CREATED.value();

    private final Map<String, IdempotencyRecord> records = new ConcurrentHashMap<>();
    private final long processingDelayMillis;

    public PaymentService(@Value("${payment.processing.delay-ms:2000}") long processingDelayMillis) {
        this.processingDelayMillis = processingDelayMillis;
    }

    public PaymentProcessingResult processPayment(String idempotencyKey, PaymentRequest paymentRequest) {
        String normalizedKey = requireIdempotencyKey(idempotencyKey);
        PaymentRequest request = Objects.requireNonNull(paymentRequest, "paymentRequest must not be null");
        String requestFingerprint = createRequestFingerprint(request);

        IdempotencyRecord candidateRecord = IdempotencyRecord.inProgress(normalizedKey, requestFingerprint);
        IdempotencyRecord existingRecord = records.putIfAbsent(normalizedKey, candidateRecord);

        if (existingRecord != null) {
            return resolveExistingRequest(existingRecord, requestFingerprint);
        }

        return handleNewRequest(candidateRecord, request);
    }

    private PaymentProcessingResult resolveExistingRequest(IdempotencyRecord existingRecord, String requestFingerprint) {
        if (!existingRecord.matchesRequest(requestFingerprint)) {
            throw new IdempotencyConflictException("Idempotency key already used for a different request body.");
        }

        if (existingRecord.isCompleted()) {
            return replay(existingRecord.getRequiredStoredResponse());
        }

        if (existingRecord.isFailed()) {
            throw new PaymentProcessingException(
                    existingRecord.getFailureReason().orElse("The original payment request failed."));
        }

        return waitForCompletion(existingRecord);
    }

    private PaymentProcessingResult handleNewRequest(IdempotencyRecord record, PaymentRequest request) {
        try {
            Thread.sleep(processingDelayMillis);

            PaymentResponse responseBody = new PaymentResponse(
                    request.chargeMessage(),
                    record.getIdempotencyKey(),
                    Instant.now());

            StoredResponse storedResponse = new StoredResponse(RESPONSE_STATUS, responseBody);
            record.complete(storedResponse);

            return new PaymentProcessingResult(storedResponse, false);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            record.fail(exception);
            records.remove(record.getIdempotencyKey(), record);
            throw new PaymentProcessingException("Payment processing was interrupted.", exception);
        } catch (RuntimeException exception) {
            record.fail(exception);
            records.remove(record.getIdempotencyKey(), record);
            throw exception;
        }
    }

    private PaymentProcessingResult waitForCompletion(IdempotencyRecord existingRecord) {
        try {
            return replay(existingRecord.getCompletionFuture().get());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Waiting for the original payment request was interrupted.", exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            throw new PaymentProcessingException("The original payment request failed while processing.", cause);
        }
    }

    private PaymentProcessingResult replay(StoredResponse storedResponse) {
        return new PaymentProcessingResult(storedResponse, true);
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException("Idempotency-Key header is required.");
        }

        return idempotencyKey.trim();
    }

    private String createRequestFingerprint(PaymentRequest request) {
        return request.amount().stripTrailingZeros().toPlainString() + ":" + request.normalizedCurrency();
    }
}
