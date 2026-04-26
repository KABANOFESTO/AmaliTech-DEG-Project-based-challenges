package rw.Pulse_Check.Pulse_Check.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import rw.Pulse_Check.Pulse_Check.model.Monitor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonitorResponse(
        String id,
        long timeout,
        @JsonProperty("alert_email")
        String alertEmail,
        String status,
        @JsonProperty("last_heartbeat_at")
        Instant lastHeartbeatAt,
        @JsonProperty("expires_at")
        Instant expiresAt,
        @JsonProperty("created_at")
        Instant createdAt,
        @JsonProperty("paused_at")
        Instant pausedAt,
        @JsonProperty("down_at")
        Instant downAt,
        @JsonProperty("remaining_seconds")
        long remainingSeconds) {

    public static MonitorResponse from(Monitor monitor) {
        return new MonitorResponse(
                monitor.getId(),
                monitor.getTimeoutSeconds(),
                monitor.getAlertEmail(),
                monitor.getStatus().name().toLowerCase(),
                monitor.getLastHeartbeatAt(),
                monitor.getExpiresAt(),
                monitor.getCreatedAt(),
                monitor.getPausedAt().orElse(null),
                monitor.getDownAt().orElse(null),
                monitor.getRemainingSeconds());
    }
}
