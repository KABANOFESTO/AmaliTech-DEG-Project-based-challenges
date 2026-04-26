package rw.idempotency_gateway.idempotency_gateway.model;

import java.time.Instant;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class IdempotencyRecord {

    private final String idempotencyKey;
    private final String requestHash;
    private final Instant createdAt;
    private final CompletableFuture<StoredResponse> completionFuture;

    private volatile IdempotencyStatus status;
    private volatile StoredResponse storedResponse;
    private volatile Instant completedAt;
    private volatile String failureReason;

    private IdempotencyRecord(String idempotencyKey, String requestHash) {
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        this.requestHash = Objects.requireNonNull(requestHash, "requestHash must not be null");
        this.createdAt = Instant.now();
        this.completionFuture = new CompletableFuture<>();
        this.status = IdempotencyStatus.IN_PROGRESS;
    }

    public static IdempotencyRecord inProgress(String idempotencyKey, String requestHash) {
        return new IdempotencyRecord(idempotencyKey, requestHash);
    }

    public synchronized void complete(StoredResponse response) {
        if (status == IdempotencyStatus.COMPLETED) {
            return;
        }

        if (status == IdempotencyStatus.FAILED) {
            throw new IllegalStateException("Cannot complete a failed idempotency record.");
        }

        this.storedResponse = Objects.requireNonNull(response, "response must not be null");
        this.completedAt = Instant.now();
        this.status = IdempotencyStatus.COMPLETED;
        this.completionFuture.complete(response);
    }

    public synchronized void fail(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable must not be null");

        if (status == IdempotencyStatus.COMPLETED) {
            throw new IllegalStateException("Cannot fail a completed idempotency record.");
        }

        if (status == IdempotencyStatus.FAILED) {
            return;
        }

        this.failureReason = throwable.getMessage();
        this.completedAt = Instant.now();
        this.status = IdempotencyStatus.FAILED;

        if (!completionFuture.isDone()) {
            completionFuture.completeExceptionally(throwable);
        }
    }

    public boolean matchesRequest(String incomingRequestHash) {
        return requestHash.equals(Objects.requireNonNull(incomingRequestHash, "incomingRequestHash must not be null"));
    }

    public boolean isCompleted() {
        return status == IdempotencyStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == IdempotencyStatus.IN_PROGRESS;
    }

    public boolean isFailed() {
        return status == IdempotencyStatus.FAILED;
    }

    public boolean hasStoredResponse() {
        return storedResponse != null;
    }

    public StoredResponse getRequiredStoredResponse() {
        if (storedResponse == null) {
            throw new IllegalStateException("Stored response is not available yet.");
        }
        return storedResponse;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public CompletableFuture<StoredResponse> getCompletionFuture() {
        return completionFuture;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public StoredResponse getStoredResponse() {
        return storedResponse;
    }

    public Optional<String> getFailureReason() {
        return Optional.ofNullable(failureReason);
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
