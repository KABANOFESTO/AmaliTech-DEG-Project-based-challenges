package rw.Pulse_Check.Pulse_Check.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class Monitor {

    private final String id;
    private final long timeoutSeconds;
    private final String alertEmail;
    private final Instant createdAt;

    private volatile MonitorStatus status;
    private volatile Instant lastHeartbeatAt;
    private volatile Instant expiresAt;
    private volatile Instant pausedAt;
    private volatile Instant downAt;

    private Monitor(String id, long timeoutSeconds, String alertEmail) {
        this.id = requireText(id, "id");
        this.timeoutSeconds = requirePositiveTimeout(timeoutSeconds);
        this.alertEmail = requireText(alertEmail, "alertEmail");
        this.createdAt = Instant.now();
        this.lastHeartbeatAt = createdAt;
        this.expiresAt = createdAt.plusSeconds(timeoutSeconds);
        this.status = MonitorStatus.ACTIVE;
    }

    public static Monitor create(String id, long timeoutSeconds, String alertEmail) {
        return new Monitor(id, timeoutSeconds, alertEmail);
    }

    public synchronized void heartbeat() {
        Instant now = Instant.now();
        this.lastHeartbeatAt = now;
        this.expiresAt = now.plusSeconds(timeoutSeconds);
        this.pausedAt = null;
        this.downAt = null;
        this.status = MonitorStatus.ACTIVE;
    }

    public synchronized void pause() {
        if (status == MonitorStatus.DOWN) {
            throw new IllegalStateException("A down monitor cannot be paused.");
        }

        if (status == MonitorStatus.PAUSED) {
            return;
        }

        this.pausedAt = Instant.now();
        this.status = MonitorStatus.PAUSED;
    }

    public synchronized void markDown() {
        if (status == MonitorStatus.DOWN) {
            return;
        }

        this.downAt = Instant.now();
        this.status = MonitorStatus.DOWN;
    }

    public boolean isExpired() {
        return status != MonitorStatus.PAUSED && expiresAt.isBefore(Instant.now());
    }

    public boolean isActive() {
        return status == MonitorStatus.ACTIVE;
    }

    public boolean isPaused() {
        return status == MonitorStatus.PAUSED;
    }

    public boolean isDown() {
        return status == MonitorStatus.DOWN;
    }

    public long getRemainingSeconds() {
        if (status == MonitorStatus.PAUSED) {
            return -1;
        }

        long remaining = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(remaining, 0);
    }

    public String getAlertMessage() {
        return "Device " + id + " is down!";
    }

    public String getId() {
        return id;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getAlertEmail() {
        return alertEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MonitorStatus getStatus() {
        return status;
    }

    public Instant getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Optional<Instant> getPausedAt() {
        return Optional.ofNullable(pausedAt);
    }

    public Optional<Instant> getDownAt() {
        return Optional.ofNullable(downAt);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }

        return value.trim();
    }

    private static long requirePositiveTimeout(long timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than zero.");
        }

        return timeoutSeconds;
    }
}
